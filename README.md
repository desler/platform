#avit-wh-rd
支持各种后门数据读取

```java
final ObtainSystem system = ObtainSystem.getInstance(SystemInfo.class);
        system.addSystem(new AMGSystem())// 厂家自定义 system 相关参数
                /**
                 * 从 androidmanifest 读取相应参数
                 */
                .addSystem(new ManifestSystem(this))
                /**
                 * 从 android 属性系统 读取相应参数
                 */
                .addSystem(new PropertiesSystem())// backdoor from properties
                /**
                 * 从 USB 存储 读取相应参数
                 */
                .bindUSBReceiverRegister(USBReceiver.getRegister().register(this))
                .addSystem(new USBSystem(this))//backdoor from USB
                /**
                 * 从文件读取
                 */
                .addSystem(FileSystem.create(this, ""))
                .setComplete(new ISystem.Complete() {
                    @Override
                    public void onComplete() {

                    }
                })
                .setError(new ISystem.Error() {
                    @Override
                    public void onError(String error) {
                        system.clear();
                        system.
                                addSystem(new AMGSystem()).
                                obtain();
                    }
                })
                .obtain(new ISystem.Callback() {
                    @Override
                    public void onCallback(Object value) {
                        initSystemConfig((SystemInfo) value);
                    }
                });
    }

    private void initSystemConfig(SystemInfo value) {

    }
```