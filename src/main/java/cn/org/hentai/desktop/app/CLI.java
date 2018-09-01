package cn.org.hentai.desktop.app;

import cn.org.hentai.desktop.system.LocalComputer;
import cn.org.hentai.desktop.system.worker.BaseWorker;
import cn.org.hentai.desktop.system.worker.CaptureWorker;
import cn.org.hentai.desktop.system.worker.CompressWorker;
import cn.org.hentai.desktop.util.Configs;
import cn.org.hentai.desktop.util.NonceStr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by matrixy on 2018/8/31.
 */
public class CLI extends Thread
{
    static String password;

    BaseWorker captureWorker = null;
    BaseWorker compressWorker = null;

    String state = "idle";

    private void interact() throws Exception
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("*********************************************************");
        System.out.println("*                                                       *");
        System.out.println("*                   Hentai CLI                          *");
        System.out.println("*                                                       *");
        System.out.println("*********************************************************");

        ArrayList<String> ipAddresses = LocalComputer.getLocalIP();
        System.out.println(String.format("server started at: %d, share the desktop with following urls: ", Configs.getInt("server.port", 5656)));
        for (String addr : ipAddresses)
        {
            echo(String.format("  http://%s:%d/", addr, Configs.getInt("server.port", 5656)));
        }
        password = NonceStr.generate(8);
        echo(String.format("access password: %s", password));

        loop : while (true)
        {
            System.out.print("> ");
            String text = reader.readLine().trim();
            boolean valid = true;
            if ("start".equals(text))
            {
                // 开始共享
                if (!"idle".equals(state))
                {
                    echo(String.format("server state: %s, command not executed", state));
                    continue loop;
                }
                state = "start";
                captureWorker = new CaptureWorker();
                compressWorker = new CompressWorker();

                captureWorker.start();
                compressWorker.start();
                echo("start sharing the desktop...");
            }
            else if ("stop".equals(text))
            {
                // TODO: 停止共享
                if (!"start".equals(state))
                {
                    echo(String.format("server state: %s, command not executed", state));
                    continue loop;
                }
                state = "idle";
                captureWorker.terminate();
                compressWorker.terminate();
                echo("stop the desktop sharing...");
            }
            else if ("list".equals(text))
            {
                // TODO: 列出全部听众
                echo("all listening members:");
            }
            else if (text.matches("^password\\s+(\\w{8})$"))
            {
                // TODO: 设定访问密码
                password = text.replaceAll("^password\\s+(\\w{8})$", "$1");
                echo("new password applied");
            }
            else if (text.matches("^kick \\d+$"))
            {
                // TODO: 踢出指定的围观群众
                String id = text.replaceAll("^kick (\\d+)$", "$1");
                echo("kick the member: " + id);
            }
            else if (text.matches("^\\d+$"))
            {
                // TODO: 由当前的模式决定是做什么

                // TODO: 由处理结果决定valid的结果
            }
            else if ("exit".equals(text))
            {
                echo("exiting...");
                // TODO: 关闭所有应该要关闭的东西
                System.exit(0);
            }
            else if ("".equals(text))
            {
                // do nothing here...
            }
            else
            {
                valid = false;
            }
            if (!valid)
            {
                echo("Commands: ");
                echo("start - start to share the desktop");
                echo("stop - stop the desktop sharing");
                echo("list - list all members");
                echo("kick <id> - kick the member with <id>");
            }
        }
    }

    private void echo(String text)
    {
        System.out.println("  " + text);
    }

    public void run()
    {
        try
        {
            interact();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static String getPassword()
    {
        return password;
    }

    public static void main(String[] args) throws Exception
    {
        new CLI().start();
    }
}
