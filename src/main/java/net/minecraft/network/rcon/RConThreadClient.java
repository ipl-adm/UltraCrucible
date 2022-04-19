package net.minecraft.network.rcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.SERVER)
public class RConThreadClient extends RConThreadBase
{
    private static final Logger field_164005_h = LogManager.getLogger();
    private boolean loggedIn;
    private Socket clientSocket;
    private byte[] buffer = new byte[1460];
    private String rconPassword;
    private static final String __OBFID = "CL_00001804";

    RConThreadClient(IServer p_i1537_1_, Socket p_i1537_2_)
    {
        super(p_i1537_1_, "RCON Client");
        this.clientSocket = p_i1537_2_;

        try
        {
            this.clientSocket.setSoTimeout(0);
        }
        catch (Exception exception)
        {
            this.running = false;
        }

        this.rconPassword = p_i1537_1_.getStringProperty("rcon.password", "");
        this.logInfo("Rcon connection from: " + p_i1537_2_.getInetAddress());
    }

    public void run()
    {
        try
        {
            while (true)
            {
                if (!this.running || clientSocket == null)
                {
                    break;
                }

                BufferedInputStream bufferedinputstream = new BufferedInputStream(this.clientSocket.getInputStream());
                int i = bufferedinputstream.read(this.buffer, 0, 1460);
                
                if (i < 10)
                {
                    this.running = false; // Cauldron
                    return;
                }

                byte b0 = 0;
                int j = RConUtils.getBytesAsLEInt(this.buffer, 0, i);

                if (j == i - 4)
                {
                    int i1 = b0 + 4;
                    int k = RConUtils.getBytesAsLEInt(this.buffer, i1, i);
                    i1 += 4;
                    int l = RConUtils.getRemainingBytesAsLEInt(this.buffer, i1);
                    i1 += 4;

                    switch (l)
                    {
                        case 2:
                            if (this.loggedIn)
                            {
                                String s1 = RConUtils.getBytesAsString(this.buffer, i1, i);

                                try
                                {
                                    this.sendMultipacketResponse(k, this.server.handleRConCommand(s1));
                                }
                                catch (Exception exception)
                                {
                                    this.sendMultipacketResponse(k, "Error executing: " + s1 + " (" + exception.getMessage() + ")");
                                }

                                continue;
                            }

                            this.sendLoginFailedResponse();
                            continue;
                        case 3:
                            String s = RConUtils.getBytesAsString(this.buffer, i1, i);
                            int j1 = i1 + s.length();

                            if (0 != s.length() && s.equals(this.rconPassword))
                            {
                                this.loggedIn = true;
                                this.sendResponse(k, 2, "");
                                continue;
                            }

                            this.loggedIn = false;
                            this.sendLoginFailedResponse();
                            continue;
                        default:
                            this.sendMultipacketResponse(k, String.format("Unknown request %s", new Object[] {Integer.toHexString(l)}));
                            continue;
                    }
                }
            }
            }
            catch (SocketTimeoutException sockettimeoutexception)
            {
                return;
            }
            catch (IOException ioexception)
            {
                return;
            }
            catch (Exception exception1)
            {
                field_164005_h.error("Exception whilst parsing RCON input", exception1);
            }
            finally
            {
                this.closeSocket();
            }
    }

    private void sendResponse(int p_72654_1_, int p_72654_2_, String p_72654_3_) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(1248);
        DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
        byte[] abyte = p_72654_3_.getBytes("UTF-8");
        dataoutputstream.writeInt(Integer.reverseBytes(abyte.length + 10));
        dataoutputstream.writeInt(Integer.reverseBytes(p_72654_1_));
        dataoutputstream.writeInt(Integer.reverseBytes(p_72654_2_));
        dataoutputstream.write(abyte);
        dataoutputstream.write(0);
        dataoutputstream.write(0);
        this.clientSocket.getOutputStream().write(bytearrayoutputstream.toByteArray());
    }

    private void sendLoginFailedResponse() throws IOException
    {
        this.sendResponse(-1, 2, "");
    }

    private void sendMultipacketResponse(int p_72655_1_, String p_72655_2_) throws IOException
    {
        int j = p_72655_2_.length();

        do
        {
            int k = 4096 <= j ? 4096 : j;
            this.sendResponse(p_72655_1_, 0, p_72655_2_.substring(0, k));
            p_72655_2_ = p_72655_2_.substring(k);
            j = p_72655_2_.length();
        }
        while (0 != j);
    }

    private void closeSocket()
    {
        this.running = false;
        if (null != this.clientSocket)
        {
            try
            {
                this.clientSocket.close();
            }
            catch (IOException ioexception)
            {
                this.logWarning("IO: " + ioexception.getMessage());
            }

            this.clientSocket = null;
        }
    }
}