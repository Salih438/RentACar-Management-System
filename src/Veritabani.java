import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Veritabani {
    public static final String DB_URL = "jdbc:sqlite:salih.db";
    public static String aktifKullanici = "";
    public static String aktifRol = "";

    public static void kurulumYap() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // KRİTİK GÜVENLİK GÜNCELLEMESİ: kullanici_adi TEXT UNIQUE yapıldı!
            stmt.execute("CREATE TABLE IF NOT EXISTS kullanicilar (id INTEGER PRIMARY KEY AUTOINCREMENT, kullanici_adi TEXT UNIQUE, sifre TEXT, rol TEXT, telefon TEXT, eposta TEXT, maas INTEGER, prim INTEGER, satis_adedi INTEGER, maas_durumu TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS \"Oto_KAR_Araç_Kiralama\" (\"İd\" INTEGER PRIMARY KEY, \"Model Türü\" TEXT, \"Saatlik Fiyat\" INTEGER, \"Durum \" TEXT, \"Temizlik\" TEXT, \"Sigorta_Bakim\" TEXT, \"Hasar\" TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS islem_gecmisi (id INTEGER PRIMARY KEY AUTOINCREMENT, kullanici_adi TEXT, tarih TEXT, islem_detayi TEXT)");

            // GÜNCELLENEN TABLO: satis_faturalari (tc_no ve ehliyet_no eklendi)
            stmt.execute("CREATE TABLE IF NOT EXISTS satis_faturalari ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "musteri_adi TEXT, "
                    + "tc_no TEXT, "
                    + "ehliyet_no TEXT, "
                    + "telefon TEXT, "
                    + "arac_model TEXT, "
                    + "kiralama_suresi INTEGER, "
                    + "toplam_tutar INTEGER, "
                    + "hasar_durumu TEXT, "
                    + "islem_tarihi TEXT, "
                    + "personel TEXT)");

            // Varsayılan kullanıcılar
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM kullanicilar");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO kullanicilar (kullanici_adi, sifre, rol, telefon, eposta, maas, prim, satis_adedi, maas_durumu) VALUES ('ali_personel', '1234', 'Personel', '0555 123 4567', 'ali@anadolulojistik.com', 17002, 500, 5, 'Bekliyor')");
                stmt.execute("INSERT INTO kullanicilar (kullanici_adi, sifre, rol, telefon, eposta, maas, prim, satis_adedi, maas_durumu) VALUES ('ayse_mudur', '1234', 'Yönetici', '0532 987 6543', 'ayse@anadolulojistik.com', 35000, 0, 0, 'Ödendi')");
                stmt.execute("INSERT INTO kullanicilar (kullanici_adi, sifre, rol, telefon, eposta, maas, prim, satis_adedi, maas_durumu) VALUES ('salih_patron', '1234', 'Patron', '0505 000 0000', 'salih@anadolulojistik.com', 0, 0, 0, '-')");
                stmt.execute("INSERT INTO kullanicilar (kullanici_adi, sifre, rol, telefon, eposta, maas, prim, satis_adedi, maas_durumu) VALUES ('sistem_admin', '1234', 'Admin', '0533 111 2233', 'admin@anadolulojistik.com', 25000, 0, 0, 'Ödendi')");
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void sistemeLogYaz(String islemDetayi) {
        String zaman = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO islem_gecmisi (kullanici_adi, tarih, islem_detayi) VALUES (?, ?, ?)")) {
            pstmt.setString(1, aktifKullanici);
            pstmt.setString(2, zaman);
            pstmt.setString(3, islemDetayi);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}