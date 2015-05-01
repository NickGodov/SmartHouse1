package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImportExportFilesActivity extends Activity {

	Activity mActivity;
	MyApplication mApplication;

	Spinner mMainMenuImportSpinner;
	Spinner mFormattedScreensImportSpinner;
	Spinner mPrefsImportSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_import_export_files);

		mActivity = this;
		mApplication = (MyApplication) getApplicationContext();

		Button btn_ok = (Button) findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mActivity.finish();
			}
		});

		// Спиннер меню
		mMainMenuImportSpinner = (Spinner) findViewById(R.id.main_menu_spinner);
		try {
			mMainMenuImportSpinner
					.setAdapter(new ArrayAdapter<String>(mActivity,
							android.R.layout.simple_spinner_item, getFiles()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mMainMenuImportSpinner.invalidate();

		// Спиннер окон вывода
		mFormattedScreensImportSpinner = (Spinner) findViewById(R.id.fs_spinner);
		try {
			mFormattedScreensImportSpinner
					.setAdapter(new ArrayAdapter<String>(mActivity,
							android.R.layout.simple_spinner_item, getFiles()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mFormattedScreensImportSpinner.invalidate();

		// Спиннер настроек
		mPrefsImportSpinner = (Spinner) findViewById(R.id.pref_spinner);
		try {
			mPrefsImportSpinner.setAdapter(new ArrayAdapter<String>(mActivity,
					android.R.layout.simple_spinner_item, getFiles()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPrefsImportSpinner.invalidate();

		// Импорт главного меню
		ImageButton mImportMainMenuButton = (ImageButton) findViewById(R.id.button_import_mainmenu);
		mImportMainMenuButton.setOnClickListener(mMainMenuImportListener());

		// Экспорт главного меню
		ImageButton mExportMainMenuButton = (ImageButton) findViewById(R.id.button_export_mainmenu);
		mExportMainMenuButton.setOnClickListener(mMainMenuExportListener());

		// Импорт окон вывода
		ImageButton mImportFormattedScreensButton = (ImageButton) findViewById(R.id.button_import_fs);
		mImportFormattedScreensButton
				.setOnClickListener(mFormattedScreensImportListener());

		// Экспорт окон вывода
		ImageButton mExportFormattedScreensButton = (ImageButton) findViewById(R.id.button_export_fs);
		mExportFormattedScreensButton
				.setOnClickListener(mFormattedScreensExportListener());

		// Импорт настроек
		ImageButton mImportPrefsButton = (ImageButton) findViewById(R.id.button_import_pref);
		mImportPrefsButton.setOnClickListener(mPrefsImportListener());

		// Экспорт настроек
		ImageButton mExportPrefsButton = (ImageButton) findViewById(R.id.button_export_pref);
		mExportPrefsButton.setOnClickListener(mPrefsExportListener());

	}

	// Получение списка файлов в папке sdcard/smarthouse
	private ArrayList<String> getFiles() throws IOException {
		ArrayList<String> files = new ArrayList<String>();
		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + Globals.EXTERNAL_ROOT_DIRECTORY);

		if (file.isDirectory()) {
			File[] listFile = file.listFiles();
            for (File f : listFile) {
                if (f.isDirectory()) {
                    File[] dirFiles = f.listFiles();
                    for (File dir : dirFiles) {
                        files.add(dir.getPath());
                    }
                } else {
                    files.add(f.getAbsolutePath());
                }
            }
		} // if
		return files;
	} // method

	View.OnClickListener mMainMenuExportListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String path = Globals.EXTERNAL_ROOT_DIRECTORY
						+ File.separator + Globals.INTERNAL_MENU_FILE;

				if (!mApplication.mProcessor.exportMenuTreeToExternalStorage()) {
					Notifications
							.showError(
									mActivity,
									"Ошибка экспорта. Убедитесь, что внешнее хранилище подключено и у вас есть права на запись файла");
				} else {
					Notifications.showPositiveMessage(mActivity,
							"Меню успешно экспортировано в файл " + path);
				}
			}
		};
	}

	// Слушатель для импорта главного меню. Входной аргумент - путь к выбранному
	// файлу
	View.OnClickListener mMainMenuImportListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String path = (String) mMainMenuImportSpinner.getSelectedItem();

				// Если path пустой
				if (path == null || path.trim().isEmpty()) {
					Notifications
							.showError(mActivity, "Путь к файлу не указан");
					return;
				}

				File import_file = new File(path);

				// Если path некорректный
				if (!import_file.isFile()) {
					Notifications.showError(mActivity,
							"Выбранный файл не является файлом");
					return;
				}

				if (!mApplication.mProcessor
						.importMenuTreeFromExternalStorage(path)
						|| !mApplication.mProcessor
								.saveMenuTreeToInternalStorage()) {
					Notifications
							.showError(
									mActivity,
									"Ошибка при импорте меню! Убедитесь, что выбран нужный файл или "
											+ "что выбранный файл совместим со структурой меню приложения.");
				} else {
					Notifications.showPositiveMessage(mActivity,
							"Главное меню успешно импортировано!");
				}
			}
		};
	}

	// Слушатель для импорта окон вывода. Входной аргумент - путь к выбранному
	// файлу
	View.OnClickListener mFormattedScreensImportListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String path = (String) mFormattedScreensImportSpinner
						.getSelectedItem();

				// Если path пустой
				if (path == null || path.trim().isEmpty()) {
					Notifications
							.showError(mActivity, "Путь к файлу не указан");
					return;
				}

				File import_file = new File(path);

				// Если path некорректный
				if (!import_file.isFile()) {
					Notifications.showError(mActivity,
							"Выбранный файл не является файлом");
					return;
				}

				if (!mApplication.mProcessor
						.importFormattedScreensFromExternalStorage(path)
						|| !mApplication.mProcessor
								.saveFormattedScreensToInternalStorage()) {
					Notifications
							.showError(
									mActivity,
									"Ошибка при импорте окон вывода! Убедитесь, что выбран нужный файл или "
											+ "что выбранный файл совместим со структурой окон вывода.");
				} else {
					Notifications.showPositiveMessage(mActivity,
							"Окна вывода успешно импортированы!");
				}
			}
		};
	}

	View.OnClickListener mFormattedScreensExportListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String path = Globals.EXTERNAL_ROOT_DIRECTORY
						+ File.separator + Globals.INTERNAL_FORMATTED_SCREENS_FILE;
				
				if (!mApplication.mProcessor
						.exportFormattedScreensToExternalStorage()) {
					Notifications
							.showError(
									mActivity,
									"Ошибка экспорта. Убедитесь, что внешнее хранилище подключено и у вас есть права на запись файла");
				} else {
					Notifications.showPositiveMessage(mActivity,
							"Окна вывода успешно экспортировано в файл " + path);
				}
			}
		};
	}

	// Слушатель для импорта окон вывода. Входной аргумент - путь к выбранному
	// файлу
	View.OnClickListener mPrefsImportListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String path = (String) mPrefsImportSpinner.getSelectedItem();

				// Если path пустой
				if (path == null || path.trim().isEmpty()) {
					Notifications
							.showError(mActivity, "Путь к файлу не указан");
					return;
				}

				File import_file = new File(path);

				// Если path некорректный
				if (!import_file.isFile()) {
					Notifications.showError(mActivity,
							"Выбранный файл не является файлом");
					return;
				}

				if (!mApplication.mProcessor
						.importPrefsFromExternalStorage(path)) {
					Notifications
							.showError(
									mActivity,
									"Ошибка при импорте настроек! Убедитесь, что выбран нужный файл или "
											+ "что выбранный файл совместим со структурой XML-документа.");
				} else {
					Notifications.showPositiveMessage(mActivity,
							"Настройки успешно импортированы!");
				}
			}
		};
	}

	View.OnClickListener mPrefsExportListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String path = Globals.EXTERNAL_ROOT_DIRECTORY
						+ File.separator + "preferences.xml";
				
				if (!mApplication.mProcessor.exportPrefsToExternalStorage()) {
					Notifications
							.showError(
									mActivity,
									"Ошибка экспорта. Убедитесь, что внешнее хранилище подключено и у вас есть права на запись файла");
				} else {
					Notifications.showPositiveMessage(mActivity,
							"Настройки успешно экспортированы в файл " + path);
				}
			}
		};
	}

	// View.OnClickListener mMainMenuExportListener() {
	// return new View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// }
	// };
	// }

}