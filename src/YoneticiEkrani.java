import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class YoneticiEkrani extends TemelEkran {

    private JTable personelTablosu, aracYonetimTablosu, logTablosu;
    private DefaultTableModel personelModeli, aracYonetimModeli, logModeli;
    private JLabel lblToplamArac, lblKirada, lblBosta, lblArizali;
    private JLabel lblToplamCiro, lblFaturaSayisi;

    @Override
    void yetkiPanelleriniEkle() {
        JTabbedPane sekmeler = new JTabbedPane();
        sekmeler.setFont(new Font("Arial", Font.BOLD, 14));

        sekmeler.addTab("📊 Operasyon & Filo Özeti", pnlOzetOlustur());
        sekmeler.addTab("🚗 Araç Yönetimi", pnlAracYonetimiOlustur()); // Başlık sadeleştirildi
        sekmeler.addTab("👥 Personel & Maaş Takibi", pnlPersonelOlustur());
        sekmeler.addTab("📜 Personel Hareketleri (Loglar)", pnlPersonelHareketleri());
        sekmeler.addTab("👤 Profil & Çıkış", pnlProfilOlustur());

        add(sekmeler, BorderLayout.CENTER);
        ozetVerileriniGuncelle();
    }

    // --- 1. SEKME: OPERASYON & FİLO ÖZETİ ---
    private JPanel pnlOzetOlustur() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlAracStats = new JPanel(new GridLayout(1, 4, 15, 15));
        lblToplamArac = new JLabel("0", SwingConstants.CENTER);
        lblKirada = new JLabel("0", SwingConstants.CENTER);
        lblBosta = new JLabel("0", SwingConstants.CENTER);
        lblArizali = new JLabel("0", SwingConstants.CENTER);

        pnlAracStats.add(kartOlustur("Toplam Araç", lblToplamArac, new Color(52, 152, 219)));
        pnlAracStats.add(kartOlustur("Aktif Kirada", lblKirada, new Color(46, 204, 113)));
        pnlAracStats.add(kartOlustur("Boşta (Hazır)", lblBosta, new Color(241, 196, 15)));
        pnlAracStats.add(kartOlustur("Arızalı/Bakım", lblArizali, new Color(231, 76, 60)));

        JPanel pnlFinans = new JPanel(new BorderLayout(15, 15));
        pnlFinans.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Departman Satış Performansı", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16)));
        pnlFinans.setBackground(Color.WHITE);

        JPanel pnlMetrikler = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlMetrikler.setBackground(Color.WHITE);
        pnlMetrikler.setBorder(new EmptyBorder(10, 20, 10, 20));

        lblToplamCiro = new JLabel("Ekibin Ürettiği Toplam Aylık Kazanç: 0 ₺");
        lblToplamCiro.setFont(new Font("Arial", Font.BOLD, 24));
        lblToplamCiro.setForeground(new Color(39, 174, 96));

        lblFaturaSayisi = new JLabel("Kesilen Toplam Sözleşme/Fatura (Bu Ay): 0 Adet");
        lblFaturaSayisi.setFont(new Font("Arial", Font.PLAIN, 18));
        lblFaturaSayisi.setForeground(Color.DARK_GRAY);

        pnlMetrikler.add(lblToplamCiro);
        pnlMetrikler.add(lblFaturaSayisi);

        JButton btnYenile = new JButton("Satış Özetini Güncelle");
        btnYenile.setBackground(new Color(41, 128, 185));
        btnYenile.setForeground(Color.WHITE);
        btnYenile.setFont(new Font("Arial", Font.BOLD, 14));
        btnYenile.setPreferredSize(new Dimension(200, 50));

        btnYenile.addActionListener(e -> {
            ozetVerileriniGuncelle();
            JOptionPane.showMessageDialog(this, "Veriler anlık olarak güncellendi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
        });

        pnlFinans.add(pnlMetrikler, BorderLayout.CENTER);

        JPanel pnlButonTutucu = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButonTutucu.setBackground(Color.WHITE);
        pnlButonTutucu.setBorder(new EmptyBorder(15, 0, 0, 20));
        pnlButonTutucu.add(btnYenile);
        pnlFinans.add(pnlButonTutucu, BorderLayout.EAST);

        panel.add(pnlAracStats, BorderLayout.NORTH);
        panel.add(pnlFinans, BorderLayout.CENTER);
        return panel;
    }

    private JPanel kartOlustur(String baslik, JLabel lblDeger, Color arkaPlan) {
        JPanel kart = new JPanel(new BorderLayout());
        kart.setBackground(arkaPlan);
        kart.setBorder(new EmptyBorder(25, 10, 25, 10));
        JLabel lblBaslik = new JLabel(baslik, SwingConstants.CENTER);
        lblBaslik.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblBaslik.setForeground(Color.WHITE);
        lblDeger.setFont(new Font("SansSerif", Font.BOLD, 48));
        lblDeger.setForeground(Color.WHITE);
        kart.add(lblBaslik, BorderLayout.NORTH);
        kart.add(lblDeger, BorderLayout.CENTER);
        return kart;
    }

    // --- 2. SEKME: ARAÇ YÖNETİMİ ---
    private JPanel pnlAracYonetimiOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] kolonlar = {"Araç ID", "Model Türü", "Saatlik Fiyat", "Durum"};
        aracYonetimModeli = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        aracYonetimTablosu = new JTable(aracYonetimModeli);
        aracYonetimTablosu.setRowHeight(25);
        aracYonetimTablosu.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(aracYonetimTablosu), BorderLayout.CENTER);

        JPanel pnlButonlar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnEkle = new JButton("Yeni Araç Ekle");
        btnEkle.setBackground(new Color(39, 174, 96)); btnEkle.setForeground(Color.WHITE);

        // YENİ EKLENEN FİYAT DÜZENLE BUTONU
        JButton btnDuzenle = new JButton("Fiyat Düzenle");
        btnDuzenle.setBackground(new Color(243, 156, 18)); btnDuzenle.setForeground(Color.WHITE);

        JButton btnSil = new JButton("Seçili Aracı Sil");
        btnSil.setBackground(new Color(192, 57, 43)); btnSil.setForeground(Color.WHITE);

        pnlButonlar.add(btnEkle); pnlButonlar.add(btnDuzenle); pnlButonlar.add(btnSil);
        panel.add(pnlButonlar, BorderLayout.SOUTH);

        aracYonetimTablosunuYenile();

        btnEkle.addActionListener(e -> {
            JTextField txtModel = new JTextField(); JTextField txtFiyat = new JTextField();
            Object[] mesaj = {"Araç Modeli:", txtModel, "Saatlik Kira Fiyatı (TL):", txtFiyat};
            Object[] secenekler = {"Kaydet", "İptal"};

            int secim = JOptionPane.showOptionDialog(this, mesaj, "Yeni Araç Kaydı", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, secenekler, secenekler[0]);

            if (secim == 0) {
                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO \"Oto_KAR_Araç_Kiralama\" (\"Model Türü\", \"Saatlik Fiyat\", \"Durum \", \"Temizlik\", \"Sigorta_Bakim\", \"Hasar\") VALUES (?, ?, 'Boşta', 'Temiz', 'Yapıldı', 'Hasarsız')")) {

                    pstmt.setString(1, txtModel.getText());
                    pstmt.setInt(2, Integer.parseInt(txtFiyat.getText()));
                    pstmt.executeUpdate();

                    aracYonetimTablosunuYenile(); ozetVerileriniGuncelle();
                    Veritabani.sistemeLogYaz("FİLOYA EKLENDİ: " + txtModel.getText() + " (" + txtFiyat.getText() + " TL)");
                    JOptionPane.showMessageDialog(this, "Yeni araç başarıyla filoya eklendi!");
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: Fiyat alanına sadece rakam giriniz."); }
            }
        });

        // YENİ EKLENEN AKSİYON: FİYAT DÜZENLEME
        btnDuzenle.addActionListener(e -> {
            int viewRow = aracYonetimTablosu.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Lütfen fiyatını düzenlemek istediğiniz aracı seçin!"); return; }

            int modelRow = aracYonetimTablosu.convertRowIndexToModel(viewRow);
            int id = (int) aracYonetimModeli.getValueAt(modelRow, 0);
            String modelAdi = (String) aracYonetimModeli.getValueAt(modelRow, 1);
            String eskiFiyat = aracYonetimModeli.getValueAt(modelRow, 2).toString().replace(" ₺", "");

            String yeniFiyatStr = JOptionPane.showInputDialog(this, modelAdi + " aracı için yeni saatlik fiyatı girin (TL):", eskiFiyat);

            if (yeniFiyatStr != null && !yeniFiyatStr.trim().isEmpty()) {
                try {
                    int yeniFiyat = Integer.parseInt(yeniFiyatStr.trim());
                    try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                         PreparedStatement pstmt = conn.prepareStatement("UPDATE \"Oto_KAR_Araç_Kiralama\" SET \"Saatlik Fiyat\" = ? WHERE \"İd\" = ?")) {
                        pstmt.setInt(1, yeniFiyat);
                        pstmt.setInt(2, id);
                        pstmt.executeUpdate();

                        aracYonetimTablosunuYenile();
                        ozetVerileriniGuncelle();
                        Veritabani.sistemeLogYaz("ARAÇ FİYATI GÜNCELLENDİ: " + modelAdi + " (" + eskiFiyat + " ₺ -> " + yeniFiyat + " ₺)");
                        JOptionPane.showMessageDialog(this, "Araç fiyatı başarıyla güncellendi.");
                    } catch (SQLException ex) { ex.printStackTrace(); }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Lütfen sadece rakam giriniz!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnSil.addActionListener(e -> {
            int viewRow = aracYonetimTablosu.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Lütfen silmek istediğiniz aracı seçin!"); return; }

            int modelRow = aracYonetimTablosu.convertRowIndexToModel(viewRow);
            String modelAdi = (String) aracYonetimModeli.getValueAt(modelRow, 1);
            Object[] secenekler = {"Evet", "Hayır"};
            int onay = JOptionPane.showOptionDialog(this, modelAdi + " adlı aracı filodan çıkarmak istediğinize emin misiniz?", "Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, secenekler, secenekler[1]);

            if (onay == 0) {
                int id = (int) aracYonetimModeli.getValueAt(modelRow, 0);
                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM \"Oto_KAR_Araç_Kiralama\" WHERE \"İd\" = ?")) {
                    pstmt.setInt(1, id); pstmt.executeUpdate();
                    aracYonetimTablosunuYenile(); ozetVerileriniGuncelle();
                    Veritabani.sistemeLogYaz("FİLODAN SİLİNDİ: " + modelAdi);
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        return panel;
    }

    // --- 3. SEKME: PERSONEL TABLOSU VE MAAŞLAR ---
    private JPanel pnlPersonelOlustur() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] kolonlar = {"ID", "Personel Adı", "Sabit Maaş (TL)", "Aylık Satış", "Satış Primi (TL)", "Toplam Ödenecek (TL)", "Verim", "Maaş Durumu"};
        personelModeli = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        personelTablosu = new JTable(personelModeli);
        personelTablosu.setRowHeight(25);
        personelTablosu.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(personelTablosu), BorderLayout.CENTER);

        JPanel pnlButonlar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        JButton btnMaasOde = new JButton("Seçili Personelin Maaşını Yatır");
        btnMaasOde.setBackground(new Color(39, 174, 96)); btnMaasOde.setForeground(Color.WHITE);

        pnlButonlar.add(btnMaasOde);
        panel.add(pnlButonlar, BorderLayout.SOUTH);

        personelleriTabloyaCek();

        btnMaasOde.addActionListener(e -> {
            int viewRow = personelTablosu.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Önce tablodan maaşı yatırılacak personeli seçin!"); return; }

            int modelRow = personelTablosu.convertRowIndexToModel(viewRow);

            if (personelModeli.getValueAt(modelRow, 7).equals("Ödendi")) {
                JOptionPane.showMessageDialog(this, "Bu personelin maaşı zaten yatırılmış!");
                return;
            }

            int pId = (int) personelModeli.getValueAt(modelRow, 0);
            String ad = (String) personelModeli.getValueAt(modelRow, 1);
            String toplamOdenecek = (String) personelModeli.getValueAt(modelRow, 5);

            try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE kullanicilar SET maas_durumu = 'Ödendi' WHERE id = ?")) {

                pstmt.setInt(1, pId); pstmt.executeUpdate();
                personelleriTabloyaCek();
                Veritabani.sistemeLogYaz("MAAŞ ÖDEMESİ: " + ad + " personeline " + toplamOdenecek + " maaş yatırıldı.");
                JOptionPane.showMessageDialog(this, ad + " adlı personelin maaşı ve primleri başarıyla yatırıldı.");

            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        return panel;
    }

    // --- 4. SEKME: PERSONEL HAREKETLERİ ---
    private JPanel pnlPersonelHareketleri() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] kolonlar = {"İşlem Tarihi / Saat", "İşlemi Yapan Personel", "Aksiyon (Mesai / Satış / İade)"};
        logModeli = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        logTablosu = new JTable(logModeli);
        logTablosu.setRowHeight(25);

        logTablosu.getColumnModel().getColumn(0).setPreferredWidth(150);
        logTablosu.getColumnModel().getColumn(1).setPreferredWidth(150);
        logTablosu.getColumnModel().getColumn(2).setPreferredWidth(450);

        panel.add(new JScrollPane(logTablosu), BorderLayout.CENTER);

        JButton btnYenile = new JButton("Hareketleri Yenile");
        btnYenile.addActionListener(e -> loglariGetir());
        panel.add(btnYenile, BorderLayout.SOUTH);

        loglariGetir();
        return panel;
    }

    // --- 5. SEKME: YÖNETİCİ PROFİLİ VE ÇIKIŞ YAP ---
    private JPanel pnlProfilOlustur() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        JLabel lblProfilBilgisi = new JLabel("Bilgiler Yükleniyor...");

        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM kullanicilar WHERE kullanici_adi = ?")) {
            pstmt.setString(1, Veritabani.aktifKullanici);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String htmlBilgi = "<html><center><h1 style='color:#2980b9;'>Yönetici Profil Kartı</h1><hr><br>"
                        + "<table style='font-size:14px;' cellpadding='5'>"
                        + "<tr><td align='right'><b>Kullanıcı Adı:</b></td><td>" + rs.getString("kullanici_adi") + "</td></tr>"
                        + "<tr><td align='right'><b>Giriş Şifresi:</b></td><td>" + rs.getString("sifre") + "</td></tr>"
                        + "<tr><td align='right'><b>Telefon No:</b></td><td>" + rs.getString("telefon") + "</td></tr>"
                        + "<tr><td align='right'><b>E-Posta Adresi:</b></td><td>" + rs.getString("eposta") + "</td></tr>"
                        + "<tr><td align='right'><b>Sistem Yetkisi:</b></td><td>" + rs.getString("rol") + "</td></tr>"
                        + "<tr><td align='right'><b>Maaş Durumu:</b></td><td><span style='color:green;'><b>" + rs.getString("maas_durumu") + " (" + rs.getInt("maas") + " ₺)</b></span></td></tr>"
                        + "</table><br><br><i>Şirket filosu ve personel yönetimi sizin sorumluluğunuzdadır.</i></center></html>";
                lblProfilBilgisi.setText(htmlBilgi);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JButton btnCikis = new JButton("Sistemden Güvenli Çıkış Yap");
        btnCikis.setBackground(new Color(192, 57, 43));
        btnCikis.setForeground(Color.WHITE);
        btnCikis.setFont(new Font("Arial", Font.BOLD, 14));

        btnCikis.addActionListener(e -> {
            int onay = JOptionPane.showConfirmDialog(this, "Sistemden çıkış yapmak istediğinize emin misiniz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if(onay == JOptionPane.YES_OPTION) {
                Veritabani.sistemeLogYaz("MESAİ BİTİŞİ (Sistemden çıkış yapıldı)");
                Veritabani.aktifKullanici = "";
                Veritabani.aktifRol = "";
                this.dispose();
                new GirisEkrani().setVisible(true);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblProfilBilgisi, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(30, 0, 0, 0);
        panel.add(btnCikis, gbc);

        return panel;
    }

    // --- YARDIMCI VERİTABANI İŞLEMLERİ ---
    private void ozetVerileriniGuncelle() {
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement()) {

            ResultSet rsArac = stmt.executeQuery("SELECT \"Durum \" FROM \"Oto_KAR_Araç_Kiralama\"");
            int toplam = 0, kirada = 0, bosta = 0, arizali = 0;
            while (rsArac.next()) {
                toplam++;
                String durum = rsArac.getString("Durum ");
                if (durum.equals("Kiralamada")) { kirada++; }
                else if (durum.equals("Boşta")) { bosta++; }
                else { arizali++; }
            }
            lblToplamArac.setText(String.valueOf(toplam));
            lblKirada.setText(String.valueOf(kirada));
            lblBosta.setText(String.valueOf(bosta));
            lblArizali.setText(String.valueOf(arizali));

            String buAy = new SimpleDateFormat("MM/yyyy").format(new Date());
            String sql = "SELECT SUM(toplam_tutar) AS ciro, COUNT(id) AS adet FROM satis_faturalari WHERE islem_tarihi LIKE ?";

            try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + buAy + "%");
                ResultSet rsFinans = pstmt.executeQuery();

                if (rsFinans.next()) {
                    int ciro = rsFinans.getInt("ciro");
                    int adet = rsFinans.getInt("adet");
                    lblToplamCiro.setText("Ekibin Ürettiği Toplam Aylık Kazanç: " + ciro + " ₺");
                    lblFaturaSayisi.setText("Kesilen Toplam Sözleşme/Fatura (Bu Ay): " + adet + " Adet");
                }
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void aracYonetimTablosunuYenile() {
        if (aracYonetimModeli != null) aracYonetimModeli.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM \"Oto_KAR_Araç_Kiralama\"")) {
            while (rs.next()) {
                aracYonetimModeli.addRow(new Object[]{rs.getInt("İd"), rs.getString("Model Türü"), rs.getInt("Saatlik Fiyat") + " ₺", rs.getString("Durum ")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void personelleriTabloyaCek() {
        if (personelModeli != null) personelModeli.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM kullanicilar WHERE rol = 'Personel'")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String kullaniciAdi = rs.getString("kullanici_adi");
                int maas = rs.getInt("maas");
                int satisAdedi = rs.getInt("satis_adedi");

                // 1. CİRO HESABI
                long personelCirosu = 0;
                try (PreparedStatement psCiro = conn.prepareStatement("SELECT SUM(toplam_tutar) AS pCiro FROM satis_faturalari WHERE personel = ?")) {
                    psCiro.setString(1, kullaniciAdi);
                    ResultSet rsCiro = psCiro.executeQuery();
                    if (rsCiro.next()) {
                        personelCirosu = rsCiro.getLong("pCiro");
                    }
                }

                // 2. ADİL PRİM FORMÜLÜ:
                // Ciro 0-10.000 TL arası: %1 prim
                // Ciro 10.000 TL üzeri: %2 prim
                // TAVAN SINIRI: Prim, personelin maaşının %30'unu asla geçemez (Usulsüzlüğü önler)
                double primOrani = (personelCirosu > 10000) ? 0.02 : 0.01;
                int hesaplananPrim = (int) (personelCirosu * primOrani);

                int primTavanSiniri = (int) (maas * 0.30); // Maaşın %30'u tavan
                if (hesaplananPrim > primTavanSiniri) {
                    hesaplananPrim = primTavanSiniri;
                }

                int toplamOdenecek = maas + hesaplananPrim;
                String verim = (personelCirosu > 5000) ? "⭐⭐⭐ Yüksek" : "⭐ Düşük";

                personelModeli.addRow(new Object[]{id, kullaniciAdi, maas + " ₺", satisAdedi + " Adet", hesaplananPrim + " ₺", toplamOdenecek + " ₺", verim, rs.getString("maas_durumu")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    private void loglariGetir() {
        if(logModeli != null) logModeli.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM islem_gecmisi ORDER BY id DESC")) {
            while (rs.next()) {
                logModeli.addRow(new Object[]{rs.getString("tarih"), rs.getString("kullanici_adi"), rs.getString("islem_detayi")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}