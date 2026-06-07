import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PatronEkrani extends TemelEkran {

    private JLabel lblAylikGelir, lblMaaşGideri, lblOdenecekVergi, lblNetKasa;

    // Vergi Sekmesi Değişkenleri
    private JButton btnVergiOde;
    private JLabel lblVergiDurum, lblToplamGiderTutari;
    private boolean vergiOdendiMi = false;
    private JTable giderTablosu;
    private DefaultTableModel giderModeli;

    private JTable filoOzetTablosu, aracYonetimTablosu;
    private DefaultTableModel filoOzetModeli, aracYonetimModeli;

    @Override
    void yetkiPanelleriniEkle() {
        JTabbedPane sekmeler = new JTabbedPane();
        sekmeler.setFont(new Font("Arial", Font.BOLD, 14));

        sekmeler.addTab("💰 Genel Kasa & Kar/Zarar", pnlKasaOlustur());
        sekmeler.addTab("🚗 Filo Yönetimi", pnlAracYonetimiOlustur());
        sekmeler.addTab("🏢 Şirket Giderleri ve Vergiler", pnlVergiOlustur());
        sekmeler.addTab("👤 Patron Profili & Çıkış", pnlProfilOlustur());

        add(sekmeler, BorderLayout.CENTER);
        muhasebeHesapla();
    }

    // --- 1. SEKME: KASA (GERÇEK VERİLERE GÖRE) ---
    private JPanel pnlKasaOlustur() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel pnlKartlar = new JPanel(new GridLayout(1, 4, 15, 15));
        pnlKartlar.setBackground(Color.WHITE);
        lblAylikGelir = new JLabel("0 ₺", SwingConstants.CENTER);
        lblMaaşGideri = new JLabel("0 ₺", SwingConstants.CENTER);
        lblOdenecekVergi = new JLabel("0 ₺", SwingConstants.CENTER);
        lblNetKasa = new JLabel("0 ₺", SwingConstants.CENTER);

        pnlKartlar.add(kartOlustur("Gerçekleşen Satış", lblAylikGelir, new Color(39, 174, 96)));
        pnlKartlar.add(kartOlustur("Aylık Maaş Gideri", lblMaaşGideri, new Color(231, 76, 60)));
        pnlKartlar.add(kartOlustur("Aylık KDV (%20)", lblOdenecekVergi, new Color(243, 156, 18)));
        pnlKartlar.add(kartOlustur("Kasadaki Net Kar", lblNetKasa, new Color(41, 128, 185)));

        JPanel pnlOzetTablo = new JPanel(new BorderLayout());
        pnlOzetTablo.setBackground(Color.WHITE);
        pnlOzetTablo.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Filo Performans ve Satış Özeti", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16)));

        String[] kolonlar = {"Araç ID", "Model Türü", "Mevcut Durum", "Saatlik Fiyat", "Gerçekleşen Getiri (TL)"};

        filoOzetModeli = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        filoOzetTablosu = new JTable(filoOzetModeli);
        filoOzetTablosu.setRowHeight(25);
        filoOzetTablosu.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filoOzetTablosu.setAutoCreateRowSorter(true);

        pnlOzetTablo.add(new JScrollPane(filoOzetTablosu), BorderLayout.CENTER);

        panel.add(pnlKartlar, BorderLayout.NORTH);
        panel.add(pnlOzetTablo, BorderLayout.CENTER);
        return panel;
    }

    // --- 2. SEKME: FİLO YÖNETİMİ ---
    private JPanel pnlAracYonetimiOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        String[] kolonlar = {"Araç ID", "Model Türü", "Saatlik Fiyat", "Durum"};
        aracYonetimModeli = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        aracYonetimTablosu = new JTable(aracYonetimModeli);
        aracYonetimTablosu.setRowHeight(25);
        aracYonetimTablosu.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(aracYonetimTablosu), BorderLayout.CENTER);

        JPanel pnlButonlar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlButonlar.setBackground(Color.WHITE);

        JButton btnEkle = new JButton("Yeni Araç Ekle");
        btnEkle.setBackground(new Color(39, 174, 96)); btnEkle.setForeground(Color.WHITE);

        JButton btnDuzenle = new JButton("Fiyat Düzenle");
        btnDuzenle.setBackground(new Color(243, 156, 18)); btnDuzenle.setForeground(Color.WHITE);

        JButton btnSil = new JButton("Seçili Aracı Sil");
        btnSil.setBackground(new Color(192, 57, 43)); btnSil.setForeground(Color.WHITE);

        pnlButonlar.add(btnEkle); pnlButonlar.add(btnDuzenle); pnlButonlar.add(btnSil);
        panel.add(pnlButonlar, BorderLayout.SOUTH);

        aracYonetimTablosunuYenile();

        btnEkle.addActionListener(e -> {
            JTextField txtModel = new JTextField();
            JTextField txtFiyat = new JTextField();
            Object[] mesaj = {"Araç Modeli:", txtModel, "Saatlik Kira Fiyatı (TL):", txtFiyat};
            Object[] secenekler = {"Kaydet", "İptal"};

            int secim = JOptionPane.showOptionDialog(this, mesaj, "Yeni Araç Alımı", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, secenekler, secenekler[0]);

            if (secim == 0) {
                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO \"Oto_KAR_Araç_Kiralama\" (\"Model Türü\", \"Saatlik Fiyat\", \"Durum \", \"Temizlik\", \"Sigorta_Bakim\", \"Hasar\") VALUES (?, ?, 'Boşta', 'Temiz', 'Yapıldı', 'Hasarsız')")) {

                    pstmt.setString(1, txtModel.getText());
                    pstmt.setInt(2, Integer.parseInt(txtFiyat.getText()));
                    pstmt.executeUpdate();

                    aracYonetimTablosunuYenile();
                    muhasebeHesapla();
                    Veritabani.sistemeLogYaz("ŞİRKETE ARAÇ ALINDI: " + txtModel.getText());
                    JOptionPane.showMessageDialog(this, "Araç filoya başarıyla eklendi.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata oluştu. Fiyatı rakam olarak girin."); }
            }
        });

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
                        muhasebeHesapla();
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
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Silinecek aracı seçin!"); return; }

            int modelRow = aracYonetimTablosu.convertRowIndexToModel(viewRow);

            String modelAdi = (String) aracYonetimModeli.getValueAt(modelRow, 1);
            Object[] secenekler = {"Evet", "Hayır"};
            int onay = JOptionPane.showOptionDialog(this, "Aracı filodan çıkarmak istiyor musunuz?", "Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, secenekler, secenekler[1]);

            if (onay == 0) {
                int id = (int) aracYonetimModeli.getValueAt(modelRow, 0);
                try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM \"Oto_KAR_Araç_Kiralama\" WHERE \"İd\" = ?")) {
                    pstmt.setInt(1, id); pstmt.executeUpdate();

                    aracYonetimTablosunuYenile();
                    muhasebeHesapla();
                    Veritabani.sistemeLogYaz("ARAÇ SATILDI/SİLİNDİ: " + modelAdi);
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        return panel;
    }

    // --- 3. SEKME: VERGİ VE GİDER EKRANI (DÖNGÜSEL TAKİP EKLENDİ) ---
    private JPanel pnlVergiOlustur() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel lblBaslik = new JLabel("Aylık Şirket Giderleri ve Vergi Dökümü", SwingConstants.CENTER);
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(lblBaslik, BorderLayout.NORTH);

        String[] kolonlar = {"Gider Kalemi", "Detay / Açıklama", "Tutar (TL)"};
        giderModeli = new DefaultTableModel(kolonlar, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        giderTablosu = new JTable(giderModeli);
        giderTablosu.setRowHeight(30);
        giderTablosu.setFont(new Font("SansSerif", Font.PLAIN, 14));

        giderTablosu.getColumnModel().getColumn(0).setPreferredWidth(150);
        giderTablosu.getColumnModel().getColumn(1).setPreferredWidth(400);
        giderTablosu.getColumnModel().getColumn(2).setPreferredWidth(100);

        panel.add(new JScrollPane(giderTablosu), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new BorderLayout(10, 10));
        pnlAlt.setBackground(Color.WHITE);
        pnlAlt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Ödeme İşlemleri"), new EmptyBorder(10, 10, 10, 10)));

        JPanel pnlBilgi = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlBilgi.setBackground(Color.WHITE);

        lblToplamGiderTutari = new JLabel("Toplam Aylık Gider: Yükleniyor...");
        lblToplamGiderTutari.setFont(new Font("Arial", Font.BOLD, 18));
        lblToplamGiderTutari.setForeground(new Color(192, 57, 43));

        lblVergiDurum = new JLabel("Durum: Yükleniyor...");
        lblVergiDurum.setFont(new Font("Arial", Font.BOLD, 14));

        pnlBilgi.add(lblToplamGiderTutari);
        pnlBilgi.add(lblVergiDurum);

        btnVergiOde = new JButton("Tümünü Öde");
        btnVergiOde.setFont(new Font("Arial", Font.BOLD, 14));
        btnVergiOde.setBackground(new Color(39, 174, 96));
        btnVergiOde.setForeground(Color.WHITE);
        btnVergiOde.setPreferredSize(new Dimension(150, 40));

        // SİSTEME GİRİŞTE BU AYIN ÖDEME KONTROLÜNÜ YAP
        if (buAyOdemeYapildiMi()) {
            vergiOdendiMi = true;
            btnVergiOde.setEnabled(false);
            btnVergiOde.setText("Ödeme Tamamlandı");
            btnVergiOde.setBackground(Color.GRAY);
            lblVergiDurum.setText("Durum: " + new SimpleDateFormat("MM/yyyy").format(new Date()) + " dönemi başarıyla ödendi.");
            lblVergiDurum.setForeground(new Color(39, 174, 96));
        } else {
            lblVergiDurum.setText("Durum: Bu ayın ödemesi bekliyor");
            lblVergiDurum.setForeground(Color.RED);
        }

        pnlAlt.add(pnlBilgi, BorderLayout.CENTER);
        pnlAlt.add(btnVergiOde, BorderLayout.EAST);

        btnVergiOde.addActionListener(e -> {
            if (!vergiOdendiMi) {
                Object[] secenekler = {"Evet", "Hayır"};
                int onay = JOptionPane.showOptionDialog(this, "Tüm giderleri ve vergileri ödemek istiyor musunuz?", "Ödeme Onayı",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, secenekler, secenekler[0]);

                if (onay == 0) {
                    vergiOdendiMi = true;
                    lblVergiDurum.setText("Durum: " + new SimpleDateFormat("MM/yyyy").format(new Date()) + " dönemi başarıyla ödendi.");
                    lblVergiDurum.setForeground(new Color(39, 174, 96));
                    btnVergiOde.setEnabled(false);
                    btnVergiOde.setText("Ödeme Tamamlandı");
                    btnVergiOde.setBackground(Color.GRAY);

                    // Bu log cümlesi veritabanında arandığı için değiştirilmemelidir.
                    Veritabani.sistemeLogYaz("ŞİRKET GİDERİ: Aylık vergi ve sabit giderler ödendi.");
                    JOptionPane.showMessageDialog(this, "Ödeme Başarılı. Kasa güncellendi.");
                }
            }
        });

        panel.add(pnlAlt, BorderLayout.SOUTH);
        return panel;
    }

    // --- 4. SEKME: PROFİL VE ÇIKIŞ ---
    private JPanel pnlProfilOlustur() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        JLabel lblProfil = new JLabel("Yükleniyor...");

        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM kullanicilar WHERE kullanici_adi = ?")) {
            pstmt.setString(1, Veritabani.aktifKullanici);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String html = "<html><center><h1 style='color:#8e44ad;'>Kurucu / Patron Kartı</h1><hr><br>"
                        + "<table style='font-size:16px;' cellpadding='5'>"
                        + "<tr><td align='right'><b>Kullanıcı Adı:</b></td><td>" + rs.getString("kullanici_adi") + "</td></tr>"
                        + "<tr><td align='right'><b>Sistem Şifresi:</b></td><td>" + rs.getString("sifre") + "</td></tr>"
                        + "<tr><td align='right'><b>Telefon No:</b></td><td>" + rs.getString("telefon") + "</td></tr>"
                        + "<tr><td align='right'><b>E-Posta:</b></td><td>" + rs.getString("eposta") + "</td></tr>"
                        + "</table><br><br><i>Şirketin tüm mali ve idari hakları size aittir.</i></center></html>";
                lblProfil.setText(html);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JButton btnCikis = new JButton("Sistemden Güvenli Çıkış Yap");
        btnCikis.setBackground(new Color(192, 57, 43));
        btnCikis.setForeground(Color.WHITE);
        btnCikis.setFont(new Font("Arial", Font.BOLD, 14));

        btnCikis.addActionListener(e -> {
            Object[] secenekler = {"Evet", "Hayır"};
            int onay = JOptionPane.showOptionDialog(this, "Sistemden çıkış yapmak istiyor musunuz?", "Çıkış", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, secenekler, secenekler[0]);
            if(onay == 0) {
                Veritabani.sistemeLogYaz("MESAİ BİTİŞİ (Sistemden çıkış yapıldı)");
                Veritabani.aktifKullanici = "";
                Veritabani.aktifRol = "";
                this.dispose();
                new GirisEkrani().setVisible(true);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblProfil, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(30, 0, 0, 0); panel.add(btnCikis, gbc);

        return panel;
    }

    // --- YARDIMCI METOTLAR VE MATEMATİK ---
    private JPanel kartOlustur(String baslik, JLabel lblDeger, Color arkaPlan) {
        JPanel kart = new JPanel(new BorderLayout());
        kart.setBackground(arkaPlan);
        kart.setBorder(new EmptyBorder(25, 10, 25, 10));
        JLabel lblBaslik = new JLabel(baslik, SwingConstants.CENTER);
        lblBaslik.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblBaslik.setForeground(Color.WHITE);
        lblDeger.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblDeger.setForeground(Color.WHITE);
        kart.add(lblBaslik, BorderLayout.NORTH);
        kart.add(lblDeger, BorderLayout.CENTER);
        return kart;
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

    // YENİ EKLENEN: AYLIK ÖDEME KONTROL METODU
    private boolean buAyOdemeYapildiMi() {
        String buAy = new SimpleDateFormat("MM/yyyy").format(new Date());
        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM islem_gecmisi WHERE islem_detayi LIKE ? AND tarih LIKE ?")) {
            // Log mesajıyla birebir eşleşmesi için:
            pstmt.setString(1, "%Aylık vergi ve sabit giderler ödendi%");
            pstmt.setString(2, "%" + buAy + "%");
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void muhasebeHesapla() {
        long toplamMaasGideri = 0;
        long gercekToplamCiro = 0;
        int aracSayisi = 0;

        if (filoOzetModeli != null) filoOzetModeli.setRowCount(0);
        if (giderModeli != null) giderModeli.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(Veritabani.DB_URL);
             Statement stmt = conn.createStatement()) {

            ResultSet rsMaas = stmt.executeQuery("SELECT SUM(maas + prim) AS toplamGider FROM kullanicilar WHERE rol IN ('Personel', 'Yönetici')");
            if (rsMaas.next()) { toplamMaasGideri = rsMaas.getLong("toplamGider"); }

            ResultSet rsCiro = stmt.executeQuery("SELECT SUM(toplam_tutar) AS ciro FROM satis_faturalari");
            if (rsCiro.next()) { gercekToplamCiro = rsCiro.getLong("ciro"); }

            ResultSet rsArac = stmt.executeQuery("SELECT * FROM \"Oto_KAR_Araç_Kiralama\"");
            while (rsArac.next()) {
                aracSayisi++;
                int id = rsArac.getInt("İd");
                String model = rsArac.getString("Model Türü");
                String durum = rsArac.getString("Durum ");
                int fiyat = rsArac.getInt("Saatlik Fiyat");

                long aracGercekGetiri = 0;
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT SUM(toplam_tutar) AS aracCiro FROM satis_faturalari WHERE arac_model = ?")) {
                    pstmt.setString(1, model);
                    ResultSet rsAracCiro = pstmt.executeQuery();
                    if (rsAracCiro.next()) {
                        aracGercekGetiri = rsAracCiro.getLong("aracCiro");
                    }
                }

                filoOzetModeli.addRow(new Object[]{id, model, durum, fiyat + " ₺", aracGercekGetiri + " ₺"});
            }

            long araToplam = (long) (gercekToplamCiro / 1.20);
            long kdv = gercekToplamCiro - araToplam;
            long bakimGideri = aracSayisi * 1500;
            long sabitGider = 12500;

            long toplamGider = toplamMaasGideri + kdv + bakimGideri + sabitGider;
            long netKasa = gercekToplamCiro - toplamGider;

            lblAylikGelir.setText(gercekToplamCiro + " ₺");
            lblMaaşGideri.setText(toplamMaasGideri + " ₺");
            lblOdenecekVergi.setText(kdv + " ₺");
            lblNetKasa.setText(netKasa + " ₺");

            if (giderModeli != null) {
                giderModeli.addRow(new Object[]{"Personel Maaşları", "Aktif çalışanların net maaş ve prim ödemeleri", toplamMaasGideri + " ₺"});
                giderModeli.addRow(new Object[]{"KDV Özeti", "Gerçekleşen satışlar üzerinden toplanan KDV", kdv + " ₺"});
                giderModeli.addRow(new Object[]{"Araç Bakım & Sigorta", "Filodaki " + aracSayisi + " araç için tahmini aylık amortisman/hasar gideri", bakimGideri + " ₺"});
                giderModeli.addRow(new Object[]{"Ofis ve Operasyon", "Ofis kirası, elektrik, su, muhasebe ve yazılım altyapı giderleri", sabitGider + " ₺"});
            }

            if(lblToplamGiderTutari != null) {
                lblToplamGiderTutari.setText("Toplam Gider: " + toplamGider + " ₺");
            }

        } catch (SQLException e) { e.printStackTrace(); }

    }
}