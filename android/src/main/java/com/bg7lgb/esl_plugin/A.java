package com.bg7lgb.esl_plugin;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.nfc.tech.NfcA;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.util.Arrays;

public class A {
    byte[] a = new byte['\ue2e0'];
    public int b = 0;
    int[] c = new int[]{0, 250, 296, 400, 800, 880, 264};
    int[] d = new int[]{0, 122, 128, 300, 480, 528, 176};
    byte[] e = new byte[]{0, 19, 19, 103, 123, 123, 124};
    int[] f = new int[]{0, 250, 296, 150, 400, 484, 48};
    byte[] g = new byte[]{0, 4, 7, 10, 14, 17, 16};

    public A() {
        this.b = 0;
    }

    void a() {
        this.b = 0;
    }

    public int b() {
        return this.b;
    }

    public int a(NfcA var1, int var2, Bitmap var3) {
        try {
            Boolean var4 = false;
            var1.connect();
            var1.setTimeout(1000);
            this.b = 0;
            byte[] var7 = "WSDZ10m".getBytes();
            byte[] var5 = var1.transceive(new byte[]{48, 0});
            byte[] var6 = Arrays.copyOf(var5, 7);
            if (!Arrays.equals(var6, var7)) {
                return 0;
            }

            this.a(var1);
            var4 = this.b(var1, var2, var3);
            if (var4) {
                return 1;
            }
        } catch (IOException var8) {
            var8.printStackTrace();
            this.b = -1;
        }

        this.b = -1;
        return 0;
    }

    private void a(NfcA var1) {
        byte[] var2 = new byte[48];

        byte[] var3;
        try {
            var3 = var1.transceive(new byte[]{48, 4});
            System.arraycopy(var3, 0, var2, 0, 16);
            byte[] var4 = var1.transceive(new byte[]{48, 8});
            System.arraycopy(var4, 0, var2, 16, 16);
            byte[] var5 = var1.transceive(new byte[]{48, 12});
            System.arraycopy(var5, 0, var2, 32, 16);
        } catch (IOException var7) {
            var7.printStackTrace();
        }

        var3 = new byte[]{3, 39, -44, 15, 21, 97, 110, 100, 114, 111, 105, 100, 46, 99, 111, 109, 58, 112, 107, 103, 119, 97, 118, 101, 115, 104, 97, 114, 101, 46, 102, 101, 110, 103, 46, 110, 102, 99, 116, 97, 103, -2, 0, 0, 0, 0, 0, 0};
        if (!Arrays.equals(var3, var2)) {
            for(int var8 = 0; var8 < 11; ++var8) {
                try {
                    var1.transceive(new byte[]{-94, (byte)(var8 + 4), var3[var8 * 4], var3[var8 * 4 + 1], var3[var8 * 4 + 2], var3[var8 * 4 + 3]});
                } catch (IOException var6) {
                    var6.printStackTrace();
                }
            }
        }

    }

    public boolean b(NfcA var1, int var2, Bitmap var3) {
        try {
            byte[] var4 = var1.transceive(new byte[]{-51, 13});
            if (var4[0] == 0 && var4[1] == 0) {
                var4 = var1.transceive(new byte[]{-51, 0, this.g[var2]});
                if (var4[0] == 0 && var4[1] == 0) {
                    SystemClock.sleep(50L);
                    var4 = var1.transceive(new byte[]{-51, 1});
                    if (var4[0] == 0 && var4[1] == 0) {
                        SystemClock.sleep(20L);
                        var4 = var1.transceive(new byte[]{-51, 2});
                        if (var4[0] == 0 && var4[1] == 0) {
                            SystemClock.sleep(20L);
                            var4 = var1.transceive(new byte[]{-51, 3});
                            if (var4[0] == 0 && var4[1] == 0) {
                                SystemClock.sleep(20L);
                                var4 = var1.transceive(new byte[]{-51, 5});
                                if (var4[0] == 0 && var4[1] == 0) {
                                    SystemClock.sleep(20L);
                                    var4 = var1.transceive(new byte[]{-51, 6});
                                    if (var4[0] == 0 && var4[1] == 0) {
                                        SystemClock.sleep(100L);
                                        int[] var5 = new int[var3.getWidth() * var3.getHeight()];
                                        int var7;
                                        if (var2 != 1 && var2 != 2 && var2 != 6) {
                                            var3.getPixels(var5, 0, var3.getWidth(), 0, 0, var3.getWidth(), var3.getHeight());
                                        } else {
                                            if (var3 == null) {
                                                return false;
                                            }

                                            int var6 = var3.getWidth();
                                            var7 = var3.getHeight();
                                            Matrix var8 = new Matrix();
                                            var8.setRotate(270.0F);
                                            Bitmap var9 = Bitmap.createBitmap(var3, 0, 0, var6, var7, var8, false);
                                            var9.getPixels(var5, 0, var9.getWidth(), 0, 0, var9.getWidth(), var9.getHeight());
                                        }

                                        Log.e("EPD_high = ", " " + this.d[var2]);
                                        Log.e("EPD_width = ", " " + this.c[var2]);
                                        byte var11;
                                        int var12;
                                        int var13;
                                        if (var2 == 1) {
                                            for(var7 = 0; var7 < 250; ++var7) {
                                                for(var12 = 0; var12 < 16; ++var12) {
                                                    var11 = 0;

                                                    for(var13 = 0; var13 < 8; ++var13) {
                                                        var11 = (byte)(var11 << 1);
                                                        if ((var5[var13 + var12 * 8 + var7 * 128] & 255) > 128) {
                                                            var11 = (byte)(var11 | 1);
                                                        }
                                                    }

                                                    this.a[var7 * 16 + var12] = var11;
                                                }
                                            }
                                        } else {
                                            for(var7 = 0; var7 < this.d[var2]; ++var7) {
                                                for(var12 = 0; var12 < this.c[var2] / 8; ++var12) {
                                                    var11 = 0;

                                                    for(var13 = 0; var13 < 8; ++var13) {
                                                        var11 = (byte)(var11 << 1);
                                                        if ((var5[var13 + var12 * 8 + var7 * this.c[var2]] & 255) > 128) {
                                                            var11 = (byte)(var11 | 1);
                                                        }
                                                    }

                                                    this.a[var7 * (this.c[var2] / 8) + var12] = var11;
                                                }
                                            }
                                        }

                                        var4 = var1.transceive(new byte[]{-51, 7, 0});
                                        if (var4[0] == 0 && var4[1] == 0) {
                                            Log.e("Packet_number = ", " " + this.f[var2]);
                                            Log.e("Packet_size = ", " " + this.e[var2]);

                                            byte[] var15;
                                            for(var7 = 0; var7 < this.f[var2]; ++var7) {
                                                var15 = new byte[this.e[var2]];
                                                System.arraycopy(new byte[]{-51, 8, (byte)(this.e[var2] - 3)}, 0, var15, 0, 3);
                                                if (var2 == 6) {
                                                    for(var13 = 0; var13 < 121; ++var13) {
                                                        var15[var13 + 3] = -1;
                                                    }
                                                } else {
                                                    System.arraycopy(this.a, var7 * (this.e[var2] - 3), var15, 3, this.e[var2] - 3);
                                                }

                                                var4 = var1.transceive(var15);
                                                if (var4[0] != 0 || var4[1] != 0) {
                                                    return false;
                                                }

                                                if (var2 == 6) {
                                                    this.b = var7 * 50 / this.f[var2];
                                                } else {
                                                    this.b = var7 * 100 / this.f[var2];
                                                }

                                                SystemClock.sleep(2L);
                                            }

                                            if (var2 == 5) {
                                                byte[] var14 = new byte[113];
                                                System.arraycopy(new byte[]{-51, 8, 120}, 0, var14, 0, 3);

                                                for(var12 = 0; var12 < 110; ++var12) {
                                                    var14[var12 + 3] = -1;
                                                }

                                                var1.transceive(var14);
                                            }

                                            var4 = var1.transceive(new byte[]{-51, 24});
                                            if (var4[0] == 0 && var4[1] == 0) {
                                                if (var2 == 6) {
                                                    SystemClock.sleep(100L);
                                                    var7 = 0;

                                                    while(true) {
                                                        if (var7 >= 48) {
                                                            SystemClock.sleep(100L);
                                                            break;
                                                        }

                                                        var15 = new byte[124];
                                                        System.arraycopy(new byte[]{-51, 25, 121}, 0, var15, 0, 3);
                                                        System.arraycopy(this.a, var7 * 121, var15, 3, 121);
                                                        this.b = var7 * 50 / 48 + 51;
                                                        var4 = var1.transceive(var15);
                                                        if (var4[0] != 0 || var4[1] != 0) {
                                                            return false;
                                                        }

                                                        SystemClock.sleep(2L);
                                                        ++var7;
                                                    }
                                                }

                                                SystemClock.sleep(200L);
                                                var4 = var1.transceive(new byte[]{-51, 9});
                                                if (var4[0] == 0 && var4[1] == 0) {
                                                    SystemClock.sleep(200L);
                                                    var7 = 0;

                                                    while(true) {
                                                        ++var7;
                                                        var4 = var1.transceive(new byte[]{-51, 10});
                                                        if (var4[0] == -1 && var4[1] == 0) {
                                                            var4 = var1.transceive(new byte[]{-51, 4});
                                                            if (var4[0] == 0 && var4[1] == 0) {
                                                                this.b = 100;
                                                                return true;
                                                            }

                                                            return false;
                                                        }

                                                        if (var7 > 100) {
                                                        }

                                                        SystemClock.sleep(25L);
                                                    }
                                                } else {
                                                    return false;
                                                }
                                            } else {
                                                return false;
                                            }
                                        } else {
                                            return false;
                                        }
                                    } else {
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException var10) {
            var10.printStackTrace();
            return false;
        }
    }
}
