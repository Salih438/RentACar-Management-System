import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // 1. Veritabanını Kur (Yoksa oluşturur)
        Veritabani.kurulumYap();

        // 2. Giriş Ekranını Başlat
        SwingUtilities.invokeLater(() -> {
            new GirisEkrani().setVisible(true);
        });
    }
}