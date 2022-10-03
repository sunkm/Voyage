ls "/dev/block/by-name/" | while read i; do
        [[ "$i" = "$MODID" ]] && continue
       echo 备份$i中
      dd if=/dev/block/by-name/$i of=/sdcard/Download/images_backup/$i
      done
 rm -rf /sdcard/Download/images_backup/userdata.img