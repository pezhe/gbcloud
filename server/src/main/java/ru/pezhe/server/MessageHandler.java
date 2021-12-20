package ru.pezhe.server;

import java.nio.file.*;

import ru.pezhe.core.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Path rootDir;
    private Path currentDir;
    private boolean isAuthorised = false;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                AbstractMessage abstractMessage) throws Exception {
        CommandType type = abstractMessage.getType();
        if (!isAuthorised) {
            switch (type) {
                case AUTH_REQUEST: {
                    rootDir = Paths.get(".\\cloud\\user1").normalize();
                    currentDir = rootDir;
                    isAuthorised = true;
                    ctx.writeAndFlush(new Response(true, "OK"));
                    break;
                }
                case REG_REQUEST: {
                    rootDir = Paths.get(".\\cloud\\" + ((Request)abstractMessage).getParams()[0]).normalize();
                    if (!Files.exists(rootDir)) {
                        Files.createDirectory(rootDir);
                    }
                    currentDir = rootDir;
                    isAuthorised = true;
                    ctx.writeAndFlush(new Response(true, "OK"));}
                    break;
            }
        } else {
            switch (type) {
                case LIST_REQUEST: {
                    String parameter = ((Request)abstractMessage).getParams()[0];
                    if (parameter.equals("\\")) {
                        currentDir = rootDir;
                    } else {
                        if (!(currentDir.equals(rootDir) && parameter.equals(".."))) {
                            currentDir = currentDir.resolve(parameter).normalize();
                        }
                    }
                    ctx.writeAndFlush(new FileList(currentDir, rootDir));
                    break;
                }
                case FILE_REQUEST: {
                    try {
                        Path file = currentDir.resolve(((Request)abstractMessage).getParams()[0]);
                        ctx.writeAndFlush(new FileTransfer(file));
                    } catch (Exception e) {
                        ctx.writeAndFlush(new Response(false,
                                "Unable to download file due to server error"));
                    }
                    break;
                }
                case FILE_TRANSFER: {
                    FileTransfer fileMsg = (FileTransfer) abstractMessage;
                    Path result = currentDir.resolve(fileMsg.getFileName());
                    if (Files.exists(result)) {
                        ctx.writeAndFlush(new Response(false,
                                "Unable to upload file! File already exists in destination directory"));
                    } else {
                        Files.write(result, fileMsg.getBytes());
                        ctx.writeAndFlush(new Response(true,
                                "File " + fileMsg.getFileName() + " was successfully uploaded"));
                    }
                    ctx.writeAndFlush(new FileList(currentDir, rootDir));
                    break;
                }
                case MKDIR_REQUEST: {
                    String name = ((Request)abstractMessage).getParams()[0];
                    Path result = currentDir.resolve(name);
                    if (Files.exists(result)) {
                        ctx.writeAndFlush(new Response(false,
                                "Unable to create directory because it already exists"));
                    } else {
                        Files.createDirectory(result);
                        ctx.writeAndFlush(new Response(true,
                                "Directory " + name + " was successfully created"));
                    }
                    ctx.writeAndFlush(new FileList(currentDir, rootDir));
                    break;
                }
                case DEL_REQUEST: {
                    String name = ((Request)abstractMessage).getParams()[0];
                    Path result = currentDir.resolve(name);
                    try {
                        Files.delete(result);
                        ctx.writeAndFlush(new Response(true,
                                "Directory " + name + " was successfully deleted"));
                    } catch (NoSuchFileException e) {
                        ctx.writeAndFlush(new Response(false,
                                "Unable to delete directory because it is not found"));
                    } catch (DirectoryNotEmptyException e) {
                        ctx.writeAndFlush(new Response(false,
                                "Unable to delete directory because it is not empty"));
                    }
                    ctx.writeAndFlush(new FileList(currentDir, rootDir));
                    break;
                }
            }
        }
    }

}
