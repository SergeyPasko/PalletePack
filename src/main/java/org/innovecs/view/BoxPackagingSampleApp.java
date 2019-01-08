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
import java.util.Arrays;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import static org.innovecs.config.Constants.*;

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

	private CheckBox lineCheckBox;
	private CheckBox virtualCheckBox;
	private CheckBox type1CheckBox;
	private CheckBox type2CheckBox;
	private CheckBox type3CheckBox;
	private CheckBox borderCheckBox;
	private ComboBox<String> destinations = new ComboBox<>();
	private ComboBox<String> pallets = new ComboBox<>();
	private Label boxInfo = new Label();
	private TreeItem<String> rootItem = new TreeItem<String>("Dummy");
	private TreeView<String> tree = new TreeView<String>(rootItem);

	private static final double CAMERA_INITIAL_DISTANCE = -3000;
	private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
	private static final double CAMERA_INITIAL_Y_ANGLE = 180.0;
	private static final double CAMERA_INITIAL_Z_ANGLE = 225.0;
	private static final double CAMERA_NEAR_CLIP = 0.1;
	private static final double CAMERA_FAR_CLIP = 10000.0;
	private static final double CONTROL_MULTIPLIER = 0.1;
	private static final double SHIFT_MULTIPLIER = 10.0;
	private static final double MOUSE_SPEED = 0.1;
	private static final double ROTATION_SPEED = 1.0;
	private static final double TRACK_SPEED = 5.0;
	private static final double SCALE_SPEED = 10.0;

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
			List<org.innovecs.models.Box> boxs = fileService.readBoxFile(fileName);
			result = packService.allPack(boxs);
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
					double newZ = z + SCALE_SPEED * mouseDeltaX * MOUSE_SPEED * modifier;
					camera.setTranslateZ(newZ);
				} else if (me.isMiddleButtonDown()) {
					cameraXform2.t.setX(cameraXform2.t.getX() - mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
					cameraXform2.t.setY(cameraXform2.t.getY() - mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
				}
			}
		});
	}

	private void handleKeyboard(Scene scene, final Node root) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				double modifier = 1.0;

				if (event.isControlDown()) {
					modifier = CONTROL_MULTIPLIER;
				}
				if (event.isShiftDown()) {
					modifier = SHIFT_MULTIPLIER;
				}

				switch (event.getCode()) {
				case P:
					cameraXform2.t.setX(0.0);
					cameraXform2.t.setY(0.0);
					camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
					cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
					cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
					break;
				case X:
					cameraXform.rx.setAngle(cameraXform.rx.getAngle() + modifier * ROTATION_SPEED);
					break;
				case Y:
					cameraXform.ry.setAngle(cameraXform.ry.getAngle() + modifier * ROTATION_SPEED);
					break;
				case Z:
					cameraXform.rz.setAngle(cameraXform.rz.getAngle() + modifier * ROTATION_SPEED);
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
		if (!borderCheckBox.isSelected()) {
			return;
		}
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

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(new Color(0, 1, 0, 0.3));

		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(new Color(0, 0, 1, 0.3));

		final PhongMaterial greyMaterial = new PhongMaterial();
		greyMaterial.setDiffuseColor(new Color(0.5, 0.5, 0.5, 0.8));

		TreeItem<String> selectedItem = tree.getSelectionModel().getSelectedItem();
		String selectedBoxName = selectedItem == null ? null : selectedItem.getValue();

		for (BoxWrapper bw : box.getBoxsInternal()) {
			drawBoxes(bw, mainXform);
			boolean isSelectedBox = bw.getName().equals(selectedBoxName);

			Xform boxXform = new Xform();
			int[] xyz = bw.getXyz();
			BoxType bt = bw.getBoxType();
			Box boxGr = new Box(xyz[3] - xyz[0], xyz[4] - xyz[1], xyz[5] - xyz[2]);
			if (lineCheckBox.isSelected() && !isSelectedBox) {
				boxGr.setDrawMode(DrawMode.LINE);
			}
			if (!virtualCheckBox.isSelected() && bw.isVirtual() && !isSelectedBox) {
				boxGr.setVisible(false);
			}

			if (isSelectedBox && !lineCheckBox.isSelected()) {
				boxGr.setMaterial(greyMaterial);
			} else {
				switch (bt) {
				case TYPE1:
					if (!type1CheckBox.isSelected())
						continue;
					boxGr.setMaterial(redMaterial);
					break;
				case TYPE2:
				case TYPE2_BLOCK_LAST_LAYER:
					if (!type2CheckBox.isSelected())
						continue;
					boxGr.setMaterial(greenMaterial);
					break;
				case TYPE3:
				case TYPE3_BLOCK_LAST_LAYER:
					if (!type3CheckBox.isSelected())
						continue;
					boxGr.setMaterial(blueMaterial);
					break;
				default:
					throw new RuntimeException("Wrong box info input");
				}
			}

			boxXform.setTranslate((xyz[3] + xyz[0]) / 2, (xyz[4] + xyz[1]) / 2, (xyz[5] + xyz[2]) / 2);
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
		buttonOpenFile.setMinWidth(250);
		Button saveFile = new Button("Save pack boxs info");
		saveFile.setMinWidth(250);

		destinations.setMinWidth(250);
		destinations.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (destinations.getValue() == null) {
					return;
				}
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

		pallets.setMinWidth(250);
		pallets.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (pallets.getValue() == null) {
					return;
				}
				drawPallete();
				rootItem.getChildren().clear();
				List<BoxWrapper> boxs = result.get(destinations.getValue()).stream()
						.filter(bw -> bw.getName().equals(pallets.getValue())).findFirst().get().getBoxsInternal();
				addNodesToTreeView(boxs, rootItem);
			}

			private void addNodesToTreeView(List<BoxWrapper> boxs, TreeItem<String> rootItem) {
				for (BoxWrapper bw : boxs) {
					TreeItem<String> node = new TreeItem<String>(bw.getName());
					addNodesToTreeView(bw.getBoxsInternal(), node);
					rootItem.getChildren().add(node);
				}
			}
		});

		buttonOpenFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Boxs File");
				fileChooser.getExtensionFilters()
						.add(new FileChooser.ExtensionFilter("Spesial csv or psk", "*.csv", "*.psk"));
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
				drawPallete();
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

		rootItem.setExpanded(true);
		tree.setShowRoot(false);

		tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue,
					TreeItem<String> newValue) {
				if (destinations.getValue() == null || pallets.getValue() == null) {
					return;
				}
				drawPallete();
				if (newValue == null) {
					boxInfo.setText("");
				} else {

					String selectedBoxName = newValue.getValue();
					List<BoxWrapper> boxs = result.get(destinations.getValue());
					BoxWrapper box = boxs.stream().filter(bw -> pallets.getValue().equals(bw.getName()))
							.filter(bw -> BoxType.PALETTE.equals(bw.getBoxType())).findFirst().get();

					writeInfo(selectedBoxName, box);
				}
			}

			private void writeInfo(String selectedBoxName, BoxWrapper box) {
				StringBuilder sb = new StringBuilder();
				for (BoxWrapper bw : box.getBoxsInternal()) {
					if (bw.getName().equals(selectedBoxName)) {
						sb.append(" name:" + bw.getName() + LINE_SEPARATOR);
						sb.append(" type:" + bw.getBoxType() + LINE_SEPARATOR);
						sb.append(" weight:" + bw.getWeight() + LINE_SEPARATOR);
						sb.append(" xyz:" + Arrays.toString(bw.getXyz()));
						boxInfo.setText(sb.toString());
						return;
					} else {
						writeInfo(selectedBoxName, bw);
					}
				}
			}
		});

		boxInfo.setMinWidth(250);

		ToolBar toolBar = new ToolBar(buttonOpenFile, saveFile, destinations, pallets, lineCheckBox, virtualCheckBox,
				type1CheckBox, type2CheckBox, type3CheckBox, borderCheckBox, tree, boxInfo);
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

	private void drawPallete() {
		if (destinations.getValue() == null || pallets.getValue() == null) {
			return;
		}
		List<BoxWrapper> boxs = result.get(destinations.getValue());
		BoxWrapper box = boxs.stream().filter(bw -> pallets.getValue().equals(bw.getName()))
				.filter(bw -> BoxType.PALETTE.equals(bw.getBoxType())).findFirst().get();
		buildBoxsPallete(box);
	}

}
