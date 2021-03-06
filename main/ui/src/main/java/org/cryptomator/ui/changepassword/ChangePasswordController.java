package org.cryptomator.ui.changepassword;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.cryptomator.common.vaults.Vault;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.cryptomator.cryptolib.api.InvalidPassphraseException;
import org.cryptomator.ui.common.FxController;
import org.cryptomator.ui.controls.FontAwesome5IconView;
import org.cryptomator.ui.controls.NiceSecurePasswordField;
import org.cryptomator.ui.common.PasswordStrengthUtil;
import org.fxmisc.easybind.EasyBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ResourceBundle;

@ChangePasswordScoped
public class ChangePasswordController implements FxController {

	private static final Logger LOG = LoggerFactory.getLogger(ChangePasswordController.class);
	private static final String MASTERKEY_FILENAME = "masterkey.cryptomator"; // TODO: deduplicate constant declared in multiple classes

	private final Stage window;
	private final Vault vault;
	private final ObjectProperty<CharSequence> newPassword;

	public NiceSecurePasswordField oldPasswordField;
	public CheckBox finalConfirmationCheckbox;
	public Button finishButton;

	@Inject
	public ChangePasswordController(@ChangePasswordWindow Stage window, @ChangePasswordWindow Vault vault, @Named("newPassword") ObjectProperty<CharSequence> newPassword) {
		this.window = window;
		this.vault = vault;
		this.newPassword = newPassword;
	}

	@FXML
	public void initialize() {
		BooleanBinding hasNotConfirmedCheckbox = finalConfirmationCheckbox.selectedProperty().not();
		BooleanBinding isInvalidNewPassword = Bindings.createBooleanBinding(() -> newPassword.get() == null || newPassword.get().length() == 0, newPassword); 
		finishButton.disableProperty().bind(hasNotConfirmedCheckbox.or(isInvalidNewPassword));
	}

	@FXML
	public void cancel() {
		window.close();
	}

	@FXML
	public void finish() {
		try {
			CryptoFileSystemProvider.changePassphrase(vault.getPath(), MASTERKEY_FILENAME, oldPasswordField.getCharacters(), newPassword.get());
			LOG.info("Successful changed password for {}", vault.getDisplayableName());
			window.close();
		} catch (IOException e) {
			// TODO show generic error screen
			LOG.error("IO error occured during password change. Unable to perform operation.", e);
			e.printStackTrace();
		} catch (InvalidPassphraseException e) {
			// TODO shake
			LOG.info("Wrong old password.");
		}
	}

	/* Getter/Setter */

	public Vault getVault() {
		return vault;
	}
	
}
