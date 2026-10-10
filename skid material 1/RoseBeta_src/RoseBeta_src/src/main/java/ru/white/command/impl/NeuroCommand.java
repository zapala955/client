package ru.white.command.impl;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.command.Command;
import ru.white.utils.ChatUtil;
import ru.white.utils.rotation.Kd;
import ru.white.utils.rotation.Md;

@Environment(EnvType.CLIENT)
public class NeuroCommand extends Command {
   private static final List<String> SUBCOMMANDS;

   static {
      String[] result = new String[13];
      result[0] = "create";
      result[1] = "select";
      result[2] = "load";
      result[3] = "save";
      result[4] = "dir";
      result[5] = "rec";
      result[6] = "live";
      result[7] = "stoprec";
      result[8] = "stop";
      result[9] = "train";
      result[10] = "list";
      result[11] = "delete";
      result[12] = "info";
      SUBCOMMANDS = List.of(result);
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {

      return SUBCOMMANDS.stream().filter(s -> {
         int v2x = -589038404;

         int var10001x = v2x * (v2x + 1) % 2;

         if (var10001x != 0) {
            }

         return s.startsWith(subPrefix.toLowerCase());
      }).collect(Collectors.toList());
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.help();
      } else {
         kd v2 = kd.get();
         String v3 = args[0].toLowerCase();
         switch (v3.hashCode()) {
            case -1884317362:
               if (v3.equals("stoprec")) {
                  v4 = 7;
               }
               break;
            case -1352294148:

               if (v3.equals("create")) {
                  v4 = 0;
               }
               break;
            case -1335458389:
               if (v3.equals("delete")) {
                  v4 = 10;
               }
               break;
            case -906021636:
               if (v3.equals("select")) {
                  v4 = 1;
               }
               break;
            case 99469:
               if (v3.equals("dir")) {
                  v4 = 4;
               }
               break;
            case 112784:
               if (v3.equals("rec")) {
                  v4 = 5;
               }
               break;
            case 3237038:
               if (v3.equals("info")) {
                  v4 = 12;
               }
               break;
            case 3322014:
               if (v3.equals("list")) {
                  v4 = 11;
               }
               break;
            case 3322092:
               if (v3.equals("live")) {
                  v4 = 6;
               }
               break;
            case 3327206:
               if (v3.equals("load")) {
                  v4 = 2;
               }
               break;
            case 3522941:
               if (v3.equals("save")) {
                  v4 = 3;
               }
               break;
            case 3540994:
               if (v3.equals("stop")) {
                  v4 = 8;
               }
               break;
            case 110621192:
               if (v3.equals("train")) {
                  v4 = 9;
               }
         }

         switch (v4) {
            case 0:
               if (args.length < 2) {
                  zp.addChatMessage("§7Использование: §f.neuro create <имя>");
                  return;
               }

               if (v2.create(args[1])) {
                  String v11;
                  String v12 = v11 = args[1];
                  zp.addChatMessage(
                     "§a[Neuro] §7Модель §f"
                        + v12
                        + "§7 создана. Начни запись: §f.neuro rec "
                        + v11
                  );
               } else {
                  String v13;
                  String v14 = v13 = args[1];
                  zp.addChatMessage(
                     "§cМодель §f"
                        + v14
                        + "§c уже существует. Выбери: §f.neuro select "
                        + v13
                  );
               }
               break;
            case 1:
               if (args.length < 2) {
                  zp.addChatMessage("§7Использование: §f.neuro select <имя>");
                  return;
               }

               if (v2.select(args[1])) {
                  String v15 = args[1];
                  zp.addChatMessage(
                     "§a[Neuro] §7Активирована модель §f"
                        + v15
                        + " §7для ротации Neuro"
                  );
               } else {
                  String v16 = args[1];
                  zp.addChatMessage("§cМодель §f" + v16 + "§c не найдена");
               }
               break;
            case 2:
               if (args.length < 2) {
                  zp.addChatMessage("§7Использование: §f.neuro load <имя>");
                  return;
               }

               if (v2.load(args[1])) {
                  String v17 = args[1];
                  zp.addChatMessage(
                     "§a[Neuro] §7Загружена модель §f"
                        + v17
                        + " §7для ротации Neuro"
                  );
               } else {
                  String v47 = args[1];
                  String v18 = String.valueOf(md.DIR);
                  String v19 = v47;
                  zp.addChatMessage(
                     "§c[Neuro] Модель §f"
                        + v19
                        + "§c не найдена в папке "
                        + v18
                  );
               }
               break;
            case 3:
               String v43 = args.length >= 2 ? args[1] : null;
               if (v2.isRecording()) {
                  v2.stopRecording(v43);
               } else {
                  v2.save(v43);
               }
               break;
            case 4:
               try {
                  Files.createDirectories(md.DIR);
                  String v42 = md.DIR.toAbsolutePath().toString();
                  new ProcessBuilder("explorer.exe", v42).start();
                  zp.addChatMessage(
                     "§a[Neuro] §7Папка с записями открыта: §f" + v42
                  );
               } catch (Exception v35) {
                  String v21 = String.valueOf(md.DIR.toAbsolutePath());
                  zp.addChatMessage(
                     "§c[Neuro] Не удалось открыть папку: " + v21
                  );
               }
               break;
            case 5:
               String v41 = args.length >= 2 ? args[1] : null;
               v2.startDummyTrain(v41);
               break;
            case 6:
               String v40 = args.length >= 2 ? args[1] : null;
               v2.startLiveRecord(v40);
               break;
            case 7:
            case 8:
               String v39 = args.length >= 2 ? args[1] : null;
               v2.stopRecording(v39);
               break;
            case 9:
               if (v2.isRecording()) {
                  zp.addChatMessage(
                     "§7[Neuro] Запись активна. Сначала §f.neuro stoprec§7, потом §f.neuro train <имя> [эпохи]"
                  );
                  return;
               }

               String v38 = null;
               if (args.length >= 2) {
                  try {
                     v44 = Integer.parseInt(args[1]);
                  } catch (NumberFormatException v36) {
                     v38 = args[1];
                     if (args.length >= 3) {
                        try {
                           v44 = Integer.parseInt(args[2]);
                        } catch (NumberFormatException v34) {
                        }
                     }
                  }
               }

               v2.startLearn(v38, v44);
               break;
            case 10:
               if (args.length < 2) {
                  zp.addChatMessage("§7Использование: §f.neuro delete <имя>");
                  return;
               }

               if (v2.delete(args[1])) {
                  String v22 = args[1];
                  zp.addChatMessage("§c[Neuro] §7Модель §f" + v22 + "§7 удалена");
               } else {
                  String v23 = args[1];
                  zp.addChatMessage("§cМодель §f" + v23 + "§c не найдена");
               }
               break;
            case 11:
               if (v2.all().isEmpty()) {
                  zp.addChatMessage("§7Моделей нет. §f.neuro create <имя>");
                  return;
               }

               zp.addChatMessage("§a[Neuro] §7Список моделей:");

               for (md v6 : v2.all()) {
                  boolean v7 = v6 == v2.getCurrent();
                  String v45;
                  if (v6.loss < 0.0) {
                     v45 = "§cне обучена";
                  } else {
                     String v24 = String.format("%.5f", v6.loss);
                     v45 = "§aобучена §7(loss §f" + v24 + "§7)";
                  }

                  String v8 = v45;
                  String v9 = v7 ? "§a▶ " : "§7  ";
                  int v46 = v6.multiPointCount();
                  int v25 = v6.trajCount();
                  int v26 = v6.strokeCount();
                  int v27 = v46;
                  String v10 = " §8| мультипоинтов: §f"
                     + v27
                     + " §8| штрихов: §f"
                     + v26
                     + " §8| траектория: §f"
                     + v25;
                  int v30 = v6.samples;
                  String v31 = v6.name;
                  zp.addChatMessage(v9 + "§f" + v31 + " §7— §f" + v30 + " §7сэмплов | " + v8 + v10);
               }
               break;
            case 12:
               md v5 = v2.getCurrent();
               if (v5 == null) {
                  zp.addChatMessage("§7Модель не выбрана");
                  return;
               }

               String result = "§a[Neuro] §f%s§7 — образцов: §f%d§7, мультипоинтов: §f%d§7, штрихов: §f%d§7, траектория: §f%d§7, ошибка: §f%s§7, запись: §f%s§7, обучение: §f%s";
               Object[] temp1 = new Object[8];
               temp1[0] = v5.name;
               temp1[1] = v5.samples;
               temp1[2] = v5.multiPointCount();
               temp1[3] = v5.strokeCount();
               temp1[4] = v5.trajCount();
               temp1[5] = v5.loss < 0.0 ? "—" : String.format("%.4f", v5.loss);
               temp1[6] = v2.isRecording() ? v2.getRecordType().name() : "нет";
               temp1[7] = v2.isLearning() ? "да" : "нет";
               zp.addChatMessage(String.format(result, temp1));
               break;
            default:
               this.help();
         }
      }
   }

   private void help() {
      zp.addChatMessage("§a=== Rose NeuroAura ===");
      zp.addChatMessage("§71. §f.neuro create <имя> §8— создать модель");

      zp.addChatMessage(
         "§72. §f.neuro rec [имя] §8— запись на боте §7| §f.neuro live [имя] §8— запись PvP"
      );
      zp.addChatMessage(
         "§7   §f.neuro save [имя] §8— сохранить запись / модель §7| §f.neuro stoprec §8— стоп"
      );
      zp.addChatMessage(
         "§73. §f.neuro train [имя] [эпохи] §8— обучить (по умолч. 2000)"
      );
      zp.addChatMessage(
         "§74. §f.neuro load <имя> §7(или select) §8— активировать модель"
      );
      zp.addChatMessage(
         "§75. §f.neuro list §8— список §7| §f.neuro dir §8— открыть папку с записями"
      );

      zp.addChatMessage(
         StrDec.d("Xx4/PAbsRzjRfXx1t5SAhS0IEFuondF3s7+WeCcd5XG3tpgAADANppzrDiganrzdmRQWRzhw24T8k85u+RxdV6icqjVYdTupiKMA5yrUP2G5bK6+ufgbSjCGn8SWEwQ=", v3)
      );
   }

   public NeuroCommand() {
      super(
         "neuro",
         ".neuro <create|select|load|save|dir|rec|live|stoprec|train|list|delete|info> [имя] [эпохи]",
         "Нейро-ротация: модели, запись и обучение (порт Src1)"
      );
   }

   
}
