package com.untitled.factories;

import com.untitled.models.Message;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.awt.Desktop;
import java.io.File;

public class MessageFactory {

    private static final String INCOMING_BG = "#FFFFFF";
    private static final String OUTGOING_BG = "#8B5CF6";
    private static final String INCOMING_TEXT = "#1F2937";
    private static final String OUTGOING_TEXT = "#FFFFFF";
    private static final String TIME_COLOR = "#6B7280";

    public static HBox createMessageComponent(Message message) {
        switch (message.getType()) {
            case IMAGE:
                return createImageMessage(message.getFilePath(), message.getFileName(), message.getFileSize(),
                                         message.isOutgoing(), message.getFormattedTime());
            case VIDEO:
                return createVideoMessage(message.getFilePath(), message.getFileName(), message.getFileSize(),
                                          message.isOutgoing(), message.getFormattedTime());
            case FILE:
                return createFileMessage(message.getFilePath(), message.getFileName(), message.getFileSize(), 
                                         message.isOutgoing(), message.getFormattedTime());
            case TEXT:
            default:
                return createTextMessage(message.getContent(), message.isOutgoing(), 
                                         message.getFormattedTime());
        }
    }

    public static HBox createTextMessage(String text, boolean isOutgoing, String time) {
        HBox container = new HBox();
        container.setPadding(new Insets(4, 16, 4, 16));
        container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(4);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setMaxWidth(400);
        
        if (isOutgoing) {
            bubble.setStyle("-fx-background-color: " + OUTGOING_BG + "; -fx-background-radius: 16 16 4 16;");
        } else {
            bubble.setStyle("-fx-background-color: " + INCOMING_BG + "; -fx-background-radius: 16 16 16 4; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);");
        }

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("System", 14));
        if (isOutgoing) {
            messageLabel.setStyle("-fx-text-fill: white;");
        } else {
            messageLabel.setTextFill(Color.web(INCOMING_TEXT));
        }

        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("System", 11));
        if (isOutgoing) {
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        } else {
            timeLabel.setTextFill(Color.web(TIME_COLOR));
        }

        bubble.getChildren().addAll(messageLabel, timeLabel);

        if (!isOutgoing) {
            Circle avatar = createAvatar("#3B82F6");
            HBox.setMargin(avatar, new Insets(0, 8, 0, 0));
            container.getChildren().addAll(avatar, bubble);
        } else {
            container.getChildren().add(bubble);
        }

        return container;
    }

    public static HBox createImageMessage(String filePath, String fileName, String fileSize, boolean isOutgoing, String time) {
        HBox container = new HBox();
        container.setPadding(new Insets(4, 16, 4, 16));
        container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(6);
        bubble.setPadding(new Insets(6));
        
        if (isOutgoing) {
            bubble.setStyle("-fx-background-color: " + OUTGOING_BG + "; -fx-background-radius: 16 16 4 16;");
        } else {
            bubble.setStyle("-fx-background-color: " + INCOMING_BG + "; -fx-background-radius: 16 16 16 4; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);");
        }

        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-radius: 12;");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        try {
            if (filePath != null && !filePath.isEmpty()) {
                Image image = new Image(filePath, 280, 200, true, true);
                imageView.setImage(image);
                
                imageView.setOnMouseClicked(e -> openImageInViewer(filePath, fileName));
                imageView.setStyle("-fx-cursor: hand;");
            }
        } catch (Exception e) {
            imageView.setFitWidth(200);
            imageView.setFitHeight(150);
        }
        
        imageContainer.getChildren().add(imageView);

        HBox infoRow = new HBox(8);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setPadding(new Insets(4, 8, 4, 8));
        
        Label nameLabel = new Label(fileName != null ? fileName : "Image");
        nameLabel.setFont(Font.font("System", 11));
        nameLabel.setMaxWidth(180);
        if (isOutgoing) {
            nameLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
        } else {
            nameLabel.setTextFill(Color.web(TIME_COLOR));
        }
        
        Label sizeLabel = new Label(fileSize != null ? " • " + fileSize : "");
        sizeLabel.setFont(Font.font("System", 11));
        if (isOutgoing) {
            sizeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        } else {
            sizeLabel.setTextFill(Color.web(TIME_COLOR));
        }
        
        Label timeLabel = new Label("  " + time);
        timeLabel.setFont(Font.font("System", 11));
        if (isOutgoing) {
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        } else {
            timeLabel.setTextFill(Color.web(TIME_COLOR));
        }

        infoRow.getChildren().addAll(nameLabel, sizeLabel, timeLabel);

        bubble.getChildren().addAll(imageContainer, infoRow);

        if (!isOutgoing) {
            Circle avatar = createAvatar("#3B82F6");
            HBox.setMargin(avatar, new Insets(0, 8, 0, 0));
            container.getChildren().addAll(avatar, bubble);
        } else {
            container.getChildren().add(bubble);
        }

        return container;
    }
    
    private static void openImageInViewer(String filePath, String fileName) {
        try {
            Stage imageStage = new Stage();
            imageStage.setTitle(fileName != null ? fileName : "Image");
            
            Image image = new Image(filePath);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(Math.min(image.getWidth(), 800));
            imageView.setFitHeight(Math.min(image.getHeight(), 600));
            
            StackPane root = new StackPane(imageView);
            root.setStyle("-fx-background-color: #1F2937;");
            root.setPadding(new Insets(20));
            
            Scene scene = new Scene(root);
            imageStage.setScene(scene);
            imageStage.show();
        } catch (Exception e) {
            System.err.println("Could not open image: " + e.getMessage());
        }
    }

    public static HBox createVideoMessage(String filePath, String videoName, String fileSize, boolean isOutgoing, String time) {
        HBox container = new HBox();
        container.setPadding(new Insets(4, 16, 4, 16));
        container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(6);
        bubble.setPadding(new Insets(6));
        
        if (isOutgoing) {
            bubble.setStyle("-fx-background-color: " + OUTGOING_BG + "; -fx-background-radius: 16 16 4 16;");
        } else {
            bubble.setStyle("-fx-background-color: " + INCOMING_BG + "; -fx-background-radius: 16 16 16 4; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);");
        }

        StackPane videoContainer = new StackPane();
        videoContainer.setPrefSize(280, 180);
        videoContainer.setStyle("-fx-background-color: #1F2937; -fx-background-radius: 12; -fx-cursor: hand;");
        
        VBox videoBackground = new VBox();
        videoBackground.setAlignment(Pos.CENTER);
        videoBackground.setPrefSize(280, 180);
        videoBackground.setStyle("-fx-background-color: linear-gradient(to bottom, #374151, #1F2937); -fx-background-radius: 12;");
        
        StackPane playButton = new StackPane();
        playButton.setPrefSize(60, 60);
        playButton.setMaxSize(60, 60);
        playButton.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 30; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);");
        
        Label playIcon = new Label("▶");
        playIcon.setFont(Font.font("System", FontWeight.BOLD, 24));
        playIcon.setTextFill(Color.web("#8B5CF6"));
        playIcon.setTranslateX(2);
        playButton.getChildren().add(playIcon);
        
        Label sizeBadge = new Label(fileSize != null ? fileSize : "Video");
        sizeBadge.setFont(Font.font("System", 11));
        sizeBadge.setTextFill(Color.WHITE);
        sizeBadge.setPadding(new Insets(4, 8, 4, 8));
        sizeBadge.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 4;");
        StackPane.setAlignment(sizeBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(sizeBadge, new Insets(8, 8, 0, 0));
        
        videoContainer.getChildren().addAll(videoBackground, playButton, sizeBadge);
        
        final String videoPath = filePath;
        videoContainer.setOnMouseClicked(e -> openVideoFile(videoPath));

        HBox infoRow = new HBox(8);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setPadding(new Insets(4, 8, 4, 8));
        
        Label videoIcon = new Label("🎬");
        videoIcon.setFont(Font.font("System", 14));
        
        Label nameLabel = new Label(videoName != null ? videoName : "Video");
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        nameLabel.setMaxWidth(180);
        if (isOutgoing) {
            nameLabel.setStyle("-fx-text-fill: white;");
        } else {
            nameLabel.setTextFill(Color.web(INCOMING_TEXT));
        }
        
        Label timeLabel = new Label("  " + time);
        timeLabel.setFont(Font.font("System", 11));
        if (isOutgoing) {
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        } else {
            timeLabel.setTextFill(Color.web(TIME_COLOR));
        }

        infoRow.getChildren().addAll(videoIcon, nameLabel, timeLabel);

        bubble.getChildren().addAll(videoContainer, infoRow);

        if (!isOutgoing) {
            Circle avatar = createAvatar("#3B82F6");
            HBox.setMargin(avatar, new Insets(0, 8, 0, 0));
            container.getChildren().addAll(avatar, bubble);
        } else {
            container.getChildren().add(bubble);
        }

        return container;
    }
    
    private static void openVideoFile(String filePath) {
        try {
            if (filePath != null && Desktop.isDesktopSupported()) {
                String path = filePath;
                if (path.startsWith("file:/")) {
                    path = path.replace("file:/", "").replace("%20", " ");
                }
                Desktop.getDesktop().open(new File(path));
            }
        } catch (Exception e) {
            System.err.println("Could not open video: " + e.getMessage());
        }
    }

    public static HBox createFileMessage(String filePath, String fileName, String fileSize, boolean isOutgoing, String time) {
        HBox container = new HBox();
        container.setPadding(new Insets(4, 16, 4, 16));
        container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(8);
        bubble.setPadding(new Insets(12, 14, 12, 14));
        bubble.setStyle("-fx-cursor: hand;");
        
        if (isOutgoing) {
            bubble.setStyle("-fx-background-color: " + OUTGOING_BG + "; -fx-background-radius: 16 16 4 16; -fx-cursor: hand;");
        } else {
            bubble.setStyle("-fx-background-color: " + INCOMING_BG + "; -fx-background-radius: 16 16 16 4; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1); -fx-cursor: hand;");
        }

        HBox fileRow = new HBox(12);
        fileRow.setAlignment(Pos.CENTER_LEFT);

        String fileExtension = getFileExtension(fileName);
        String iconEmoji = getFileIcon(fileExtension);
        String iconBgColor = getFileIconBgColor(fileExtension, isOutgoing);
        
        VBox fileIconBox = new VBox();
        fileIconBox.setAlignment(Pos.CENTER);
        fileIconBox.setPrefSize(48, 48);
        fileIconBox.setMinSize(48, 48);
        fileIconBox.setStyle("-fx-background-color: " + iconBgColor + "; -fx-background-radius: 10;");
        Label icon = new Label(iconEmoji);
        icon.setFont(Font.font("System", 22));
        fileIconBox.getChildren().add(icon);

        VBox fileInfo = new VBox(3);
        Label nameLabel = new Label(fileName != null ? fileName : "File");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        nameLabel.setMaxWidth(200);
        if (isOutgoing) {
            nameLabel.setStyle("-fx-text-fill: white;");
        } else {
            nameLabel.setTextFill(Color.web(INCOMING_TEXT));
        }
        
        HBox metaRow = new HBox(8);
        Label sizeLabel = new Label(fileSize != null ? fileSize : "File");
        sizeLabel.setFont(Font.font("System", 11));
        if (isOutgoing) {
            sizeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        } else {
            sizeLabel.setTextFill(Color.web(TIME_COLOR));
        }
        
        Label extLabel = new Label(fileExtension.toUpperCase());
        extLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        extLabel.setPadding(new Insets(1, 6, 1, 6));
        if (isOutgoing) {
            extLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 4;");
        } else {
            extLabel.setTextFill(Color.web("#9CA3AF"));
            extLabel.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 4;");
        }
        
        metaRow.getChildren().addAll(sizeLabel, extLabel);
        fileInfo.getChildren().addAll(nameLabel, metaRow);

        fileRow.getChildren().addAll(fileIconBox, fileInfo);

        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("System", 11));
        if (isOutgoing) {
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        } else {
            timeLabel.setTextFill(Color.web(TIME_COLOR));
        }

        bubble.getChildren().addAll(fileRow, timeLabel);
        
        final String path = filePath;
        bubble.setOnMouseClicked(e -> openFile(path));

        if (!isOutgoing) {
            Circle avatar = createAvatar("#3B82F6");
            HBox.setMargin(avatar, new Insets(0, 8, 0, 0));
            container.getChildren().addAll(avatar, bubble);
        } else {
            container.getChildren().add(bubble);
        }

        return container;
    }
    
    private static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "file";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private static String getFileIcon(String extension) {
        switch (extension) {
            case "pdf": return "📕";
            case "doc": case "docx": return "📘";
            case "xls": case "xlsx": return "📗";
            case "ppt": case "pptx": return "📙";
            case "zip": case "rar": case "7z": return "🗜️";
            case "txt": return "📝";
            case "mp3": case "wav": case "aac": return "🎵";
            default: return "📄";
        }
    }
    
    private static String getFileIconBgColor(String extension, boolean isOutgoing) {
        if (isOutgoing) return "rgba(255,255,255,0.2)";
        
        switch (extension) {
            case "pdf": return "#FEE2E2";
            case "doc": case "docx": return "#DBEAFE";
            case "xls": case "xlsx": return "#DCFCE7";
            case "ppt": case "pptx": return "#FEF3C7";
            case "zip": case "rar": case "7z": return "#F3E8FF";
            default: return "#F3F4F6";
        }
    }
    
    private static void openFile(String filePath) {
        try {
            if (filePath != null && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(filePath));
            }
        } catch (Exception e) {
            System.err.println("Could not open file: " + e.getMessage());
        }
    }

    private static Circle createAvatar(String color) {
        Circle avatar = new Circle(18);
        avatar.setFill(Color.web(color));
        return avatar;
    }

    public static HBox createTypingIndicator(String userName) {
        HBox container = new HBox(8);
        container.setPadding(new Insets(8, 16, 8, 16));
        container.setAlignment(Pos.CENTER_LEFT);

        HBox dots = new HBox(4);
        dots.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Circle dot = new Circle(3);
            dot.setFill(Color.web("#6B7280"));
            dots.getChildren().add(dot);
        }

        Label label = new Label(userName + " is typing");
        label.setFont(Font.font("System", 12));
        label.setTextFill(Color.web("#6B7280"));

        container.getChildren().addAll(dots, label);
        return container;
    }

    public static HBox createDateSeparator(String date) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(16, 0, 16, 0));

        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("System", 12));
        dateLabel.setTextFill(Color.web("#6B7280"));
        dateLabel.setPadding(new Insets(4, 12, 4, 12));
        dateLabel.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 12;");

        container.getChildren().add(dateLabel);
        return container;
    }
}
