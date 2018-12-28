/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.innovecs.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.innovecs.config.Constants;
import org.innovecs.models.BoxType;
import org.innovecs.models.BoxWrapper;
import org.innovecs.services.FileService;
import org.innovecs.services.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author cmcastil
 * @author spasko
 */
public class BoxPackagingSampleApp extends Application {

	final Group root = new Group();
	final Xform moleculeGroup = new Xform();
	final Xform world = new Xform();
	final PerspectiveCamera camera = new PerspectiveCamera(true);
	final Xform cameraXform = new Xform();
	final Xform cameraXform2 = new Xform();
	final Xform cameraXform3 = new Xform();

	CheckBox lineCheckBox;
	CheckBox virtualCheckBox;
	CheckBox type1CheckBox;
	CheckBox type2CheckBox;
	CheckBox type3CheckBox;
	CheckBox borderCheckBox;

	private static final double CAMERA_INITIAL_DISTANCE = -3000;
	private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
	private static final double CAMERA_INITIAL_Y_ANGLE = 180.0;
	private static final double CAMERA_INITIAL_Z_ANGLE = 45.0;
	private static final double CAMERA_NEAR_CLIP = 0.1;
	private static final double CAMERA_FAR_CLIP = 10000.0;
	private static final double CONTROL_MULTIPLIER = 0.1;
	private static final double SHIFT_MULTIPLIER = 10.0;
	private static final double MOUSE_SPEED = 0.1;
	private static final double ROTATION_SPEED = 1.0;
	private static final double TRACK_SPEED = 0.3;

	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;

	@Autowired
	private FileService fileService;
	@Autowired
	private PackService packService;
	protected Map<String, List<BoxWrapper>> result;

	private Map<String, List<BoxWrapper>> packBoxes(String fileName) {
		if (fileName.endsWith("psk")) {
			result = fileService.readPskFile(fileName);
		} else {
			List<org.innovecs.models.Box> boxs = new ArrayList<>();
			boxs = fileService.readBoxFile(fileName);
			Map<String, List<org.innovecs.models.Box>> boxesByDestinations = boxs.stream()
					.collect(Collectors.groupingBy(org.innovecs.models.Box::getDestination));
			result = boxesByDestinations.values().stream().flatMap(b -> packService.calculatePack(b).stream())
					.collect(Collectors.groupingBy(BoxWrapper::getDestination));
		}

		return result;
	}

	private void buildCamera() {
		root.getChildren().add(cameraXform);
		cameraXform.getChildren().add(cameraXform2);
		cameraXform2.getChildren().add(cameraXform3);
		cameraXform3.getChildren().add(camera);
		cameraXform.setTranslate(Constants.PALLETE_LENGTH / 2, Constants.PALLETE_WIDTH / 2,
				Constants.PALLETE_MAXHEIGHT / 2);
		cameraXform.setRotateZ(CAMERA_INITIAL_Z_ANGLE);

		camera.setNearClip(CAMERA_NEAR_CLIP);
		camera.setFarClip(CAMERA_FAR_CLIP);
		camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
		cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
		cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
	}

	private void handleMouse(SubScene scene, final Node root) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);

				double modifier = 1.0;

				if (me.isControlDown()) {
					modifier = CONTROL_MULTIPLIER;
				}
				if (me.isShiftDown()) {
					modifier = SHIFT_MULTIPLIER;
				}
				if (me.isPrimaryButtonDown()) {
					cameraXform.ry.setAngle(
							cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
					cameraXform.rx.setAngle(
							cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
				} else if (me.isSecondaryButtonDown()) {
					double z = camera.getTranslateZ();
					double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
					camera.setTranslateZ(newZ);
				} else if (me.isMiddleButtonDown()) {
					cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
					cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
				}
			}
		});
	}

	private void handleKeyboard(Scene scene, final Node root) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case Z:
					cameraXform2.t.setX(0.0);
					cameraXform2.t.setY(0.0);
					camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
					cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
					cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
					break;
				case V:
					moleculeGroup.setVisible(!moleculeGroup.isVisible());
					break;
				default:
					break;
				}
			}
		});
	}

	private void buildBoxsPallete(BoxWrapper box) {

		Xform mainXform = new Xform();

		drawBoxes(box, mainXform);
		drawPalleteBorder(mainXform);

		moleculeGroup.getChildren().clear();
		moleculeGroup.getChildren().add(mainXform);
		world.getChildren().clear();
		world.getChildren().addAll(moleculeGroup);
	}

	private void drawPalleteBorder(Group mainXform) {
		final PhongMaterial palleteMaterial = new PhongMaterial();
		palleteMaterial.setDiffuseColor(new Color(0.5, 0.5, 0.5, 0.1));
		Xform boxXform = new Xform();
		if (!borderCheckBox.isSelected())
			return;
		Box boxGr = new Box(Constants.PALLETE_LENGTH, Constants.PALLETE_WIDTH, Constants.PALLETE_MAXHEIGHT);
		if (lineCheckBox.isSelected()) {
			boxGr.setDrawMode(DrawMode.LINE);
		}
		boxGr.setMaterial(palleteMaterial);
		boxXform.setTranslate(Constants.PALLETE_LENGTH / 2, Constants.PALLETE_WIDTH / 2,
				Constants.PALLETE_MAXHEIGHT / 2);
		mainXform.getChildren().add(boxXform);
		boxXform.getChildren().add(boxGr);
	}

	private void drawBoxes(BoxWrapper box, Xform mainXform) {
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(new Color(1, 0, 0, 0.3));

		final PhongMaterial brownMaterial = new PhongMaterial();
		brownMaterial.setDiffuseColor(new Color(0, 1, 0, 0.3));

		final PhongMaterial greyMaterial = new PhongMaterial();
		greyMaterial.setDiffuseColor(new Color(0, 0, 1, 0.3));
		for (BoxWrapper bw : box.getBoxsInternal()) {
			drawBoxes(bw, mainXform);
			Xform boxXform = new Xform();
			int[] xyz = bw.getXyz();
			BoxType bt = bw.getBoxType();
			Box boxGr = new Box(bt.getLength(), bt.getWidth(), bt.getHeight());
			if (lineCheckBox.isSelected()) {
				boxGr.setDrawMode(DrawMode.LINE);
			}
			if (!virtualCheckBox.isSelected() && bw.isVirtual()) {
				boxGr.setVisible(false);
			}

			switch (bt) {
			case TYPE1:
				if (!type1CheckBox.isSelected())
					continue;
				boxGr.setMaterial(redMaterial);
				break;
			case TYPE2:
			case TYPE_BLOCK_LAST_LAYER:
				if (!type2CheckBox.isSelected())
					continue;
				boxGr.setMaterial(brownMaterial);
				break;
			case TYPE3:
				if (!type3CheckBox.isSelected())
					continue;
				boxGr.setMaterial(greyMaterial);
				break;
			default:
				throw new RuntimeException("Wrong box info input");
			}

			boxXform.setTranslate(xyz[0] + bt.getLength() / 2, xyz[1] + bt.getWidth() / 2, xyz[2] + bt.getHeight() / 2);
			mainXform.getChildren().add(boxXform);
			boxXform.getChildren().add(boxGr);
		}
	}

	@Override
	public void start(Stage primaryStage) {

		setUserAgentStylesheet(STYLESHEET_MODENA);

		root.getChildren().add(world);
		root.setDepthTest(DepthTest.ENABLE);

		buildCamera();

		SubScene scene = new SubScene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
		scene.setFill(Color.GREY);
		handleMouse(scene, root);

		scene.setCamera(camera);

		// 2D

		BorderPane pane = new BorderPane();
		pane.setCenter(scene);
		Button buttonOpenFile = new Button("Load boxs info");
		Button saveFile = new Button("Save pack boxs info");

		ComboBox<String> destinations = new ComboBox<>();
		ComboBox<String> pallets = new ComboBox<>();
		destinations.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				pallets.getItems().clear();
				List<BoxWrapper> boxs = result.get(destinations.getValue());
				List<String> palls = boxs.stream().filter(bw -> BoxType.PALETTE.equals(bw.getBoxType()))
						.map(BoxWrapper::getName).collect(Collectors.toList());
				if (!palls.isEmpty()) {
					pallets.getItems().addAll(palls);
					pallets.setValue(palls.get(0));
				}
			}
		});

		pallets.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (pallets.getValue() == null) {
					return;
				}
				drawPallete(destinations, pallets);
			}
		});

		buttonOpenFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Boxs File");
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Only special csv", "*.csv"));
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Only special psk", "*.psk"));
				File file = fileChooser.showOpenDialog(primaryStage);
				if (file != null) {
					result = packBoxes(file.getAbsolutePath());
					destinations.getItems().clear();
					Set<String> dests = result.keySet();
					if (!dests.isEmpty()) {
						destinations.getItems().addAll(result.keySet());
						destinations.setValue(result.keySet().iterator().next());
					}
					saveFile.setDisable(false);
				}
			}
		});

		EventHandler<ActionEvent> redrawHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				drawPallete(destinations, pallets);
			}
		};

		lineCheckBox = new CheckBox("Line draw");
		lineCheckBox.setSelected(true);
		lineCheckBox.setOnAction(redrawHandler);

		virtualCheckBox = new CheckBox("Show multiplex boxs");
		virtualCheckBox.setSelected(true);
		virtualCheckBox.setOnAction(redrawHandler);

		type1CheckBox = new CheckBox("Show Type1");
		type1CheckBox.setSelected(true);
		type1CheckBox.setOnAction(redrawHandler);

		type2CheckBox = new CheckBox("Show Type2");
		type2CheckBox.setSelected(true);
		type2CheckBox.setOnAction(redrawHandler);

		type3CheckBox = new CheckBox("Show Type3");
		type3CheckBox.setSelected(true);
		type3CheckBox.setOnAction(redrawHandler);

		borderCheckBox = new CheckBox("Show pallete");
		borderCheckBox.setOnAction(redrawHandler);

		saveFile.setDisable(true);
		saveFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Only formal psk", "*.psk"));

				File file = fileChooser.showSaveDialog(primaryStage);
				if (file != null) {
					fileService.writePositonsBoxFile(file.getAbsolutePath(), result);
				}
			}
		});
		ToolBar toolBar = new ToolBar(buttonOpenFile, destinations, pallets, lineCheckBox, virtualCheckBox,
				type1CheckBox, type2CheckBox, type3CheckBox, borderCheckBox, saveFile);
		toolBar.setOrientation(Orientation.VERTICAL);
		pane.setLeft(toolBar);
		pane.setPrefSize(300, 300);

		Scene sceneMain = new Scene(pane);

		handleKeyboard(sceneMain, root);

		primaryStage.setTitle("Innovecs Boxs Packaging Sample Application");
		primaryStage.setScene(sceneMain);
		primaryStage.show();

	}

	@Override
	public void init() throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(getClass());
		context.getAutowireCapableBeanFactory().autowireBean(this);
	}

	private void drawPallete(ComboBox<String> destinations, ComboBox<String> pallets) {
		List<BoxWrapper> boxs = result.get(destinations.getValue());
		BoxWrapper box = boxs.stream().filter(bw -> pallets.getValue().equals(bw.getName()))
				.filter(bw -> BoxType.PALETTE.equals(bw.getBoxType())).findFirst().get();
		buildBoxsPallete(box);
	}

}
