import javax.swing.*;
import java.awt.*;

public abstract class TemelEkran extends JFrame {

    // Alt sınıfların (Admin, Personel vb.) kendi panellerini eklemesini zorunlu kılan soyut metot
    abstract void yetkiPanelleriniEkle();

    public TemelEkran() {
        setTitle("Anadolu Lojistik | Aktif Rol: " + Veritabani.aktifRol);

        // UI İYİLEŞTİRMESİ: Tabloların (TC, Ehliyet vb.) sığması için ekran boyutu genişletildi.
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel ustPanel = new JPanel(new BorderLayout());
        ustPanel.setBackground(new Color(52, 73, 94));
        ustPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Estetik boşluk

        JLabel lblBaslik = new JLabel("Aktif Kullanıcı: @" + Veritabani.aktifKullanici + "  |  Yetki: " + Veritabani.aktifRol);
        lblBaslik.setForeground(Color.WHITE);
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 14));
        ustPanel.add(lblBaslik, BorderLayout.WEST);

        JButton btnCikis = new JButton("Sistemden Çıkış");
        btnCikis.setBackground(new Color(231, 76, 60));
        btnCikis.setForeground(Color.WHITE);
        btnCikis.setFocusPainted(false);

        // KRİTİK GÜVENLİK HATASI ÇÖZÜLDÜ: Çıkış işlemleri standartlaştırıldı
        btnCikis.addActionListener(e -> {
            int onay = JOptionPane.showConfirmDialog(this, "Sistemden çıkış yapmak istediğinize emin misiniz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if(onay == JOptionPane.YES_OPTION) {
                // Arka kapı kapatıldı: Artık üstteki butondan çıkılsa bile log atılacak ve oturum sıfırlanacak!
                Veritabani.sistemeLogYaz("MESAİ BİTİŞİ (Sistemden çıkış yapıldı)");
                Veritabani.aktifKullanici = "";
                Veritabani.aktifRol = "";

                dispose();
                new GirisEkrani().setVisible(true);
            }
        });

        ustPanel.add(btnCikis, BorderLayout.EAST);

        add(ustPanel, BorderLayout.NORTH);


        yetkiPanelleriniEkle();
    }
}