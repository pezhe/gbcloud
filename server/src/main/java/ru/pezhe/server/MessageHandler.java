package ru.pezhe.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import ru.pezhe.core.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Path rootDir;
    private Path currentDir;
    private boolean isAuthorised = false;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        /*currentDir = Paths.get("root");
        if (!Files.exists(currentDir)) {
            Files.createDirectory(currentDir);
        }
        ctx.writeAndFlush(new FileList(currentDir));*/
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                AbstractMessage abstractMessage) throws Exception {
        CommandType type = abstractMessage.getType();
        if (!isAuthorised) {
            switch (type) {
                case AUTH_REQUEST:
                    rootDir = Paths.get("."); //to change to default user path
                    currentDir = rootDir;
                    ctx.writeAndFlush(new Response(true, "OK"));
                    isAuthorised = true;
                    break;
                case REG_REQUEST:
                    ctx.writeAndFlush(new Response(false, "Feature is not ready yet"));
            }
        } else {
            switch (type) {
                case LIST_REQUEST:
                    ctx.writeAndFlush(new FileList(collectList(Paths.get(((Request)abstractMessage).getParams()[0])),
                            getRelativePath()));
                    break;
                case FILE_REQUEST:
                    Request request = (Request) abstractMessage;
                    Path file = currentDir.resolve(request.getParams()[0]);
                    ctx.writeAndFlush(new FileTransfer(file));
                    break;
                case FILE_TRANSFER:
                    FileTransfer fileMsg = (FileTransfer) abstractMessage;
                    Files.write(
                        currentDir.resolve(fileMsg.getFileName()),
                        fileMsg.getBytes()
                    );
                    ctx.writeAndFlush(new FileList(collectList(currentDir), getRelativePath()));
                    break;
            }
        }
    }

    private List<FileInfo> collectList(Path path) throws IOException {
        List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
        if (path != rootDir) list.add(new FileInfo());
        return list;
    }

    private String getRelativePath() {
        return rootDir.relativize(currentDir).toString();
    }

}
