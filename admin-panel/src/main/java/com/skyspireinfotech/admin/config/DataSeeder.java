package com.skyspireinfotech.admin.config;

import com.skyspireinfotech.admin.entity.AdminUser;
import com.skyspireinfotech.admin.entity.ContactInfo;
import com.skyspireinfotech.admin.entity.ServiceItem;
import com.skyspireinfotech.admin.entity.SiteContent;
import com.skyspireinfotech.admin.repository.AdminUserRepository;
import com.skyspireinfotech.admin.repository.ContactInfoRepository;
import com.skyspireinfotech.admin.repository.ServiceItemRepository;
import com.skyspireinfotech.admin.repository.SiteContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin login and starter content on first run so the panel
 * isn't empty. Safe to run on every startup — each seed only writes if the
 * row doesn't already exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AdminUserRepository adminUserRepository;
    private final SiteContentRepository siteContentRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.username}")
    private String seedUsername;

    @Value("${admin.seed.password}")
    private String seedPassword;

    public DataSeeder(AdminUserRepository adminUserRepository,
                       SiteContentRepository siteContentRepository,
                       ServiceItemRepository serviceItemRepository,
                       ContactInfoRepository contactInfoRepository,
                       PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.siteContentRepository = siteContentRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.contactInfoRepository = contactInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedHomeContent();
        seedAboutContent();
        seedServices();
        seedContactInfo();
    }

    private void seedAdminUser() {
        if (adminUserRepository.count() == 0) {
            AdminUser user = new AdminUser();
            user.setUsername(seedUsername);
            user.setPassword(passwordEncoder.encode(seedPassword));
            user.setEnabled(true);
            adminUserRepository.save(user);
            log.warn("Seeded default admin user '{}' with the password from application.properties. "
                    + "Log in and change it immediately.", seedUsername);
        }
    }

    private void seedHomeContent() {
        if (siteContentRepository.findBySectionKey("HOME").isEmpty()) {
            SiteContent home = new SiteContent();
            home.setSectionKey("HOME");
            home.setHeading("We build the technology behind growing businesses");
            home.setSubheading("Web · App · Software · IT Solutions");
            home.setBodyText("Skyspire InfoTech partners with startups and enterprises to design, build, "
                    + "and support the websites, apps, and software that power their next stage of growth.");
            siteContentRepository.save(home);
        }
    }

    private void seedAboutContent() {
        if (siteContentRepository.findBySectionKey("ABOUT").isEmpty()) {
            SiteContent about = new SiteContent();
            about.setSectionKey("ABOUT");
            about.setHeading("A technology partner, not just a vendor");
            about.setSubheading("Why Skyspire InfoTech");
            about.setBodyText("We combine engineering discipline with clear communication, so projects stay "
                    + "on schedule, on budget, and aligned with what your business actually needs.");
            siteContentRepository.save(about);
        }
        backfillAboutExtras();
    }

    /**
     * Numbers and "why us" points were added later; fill only the blank ones with
     * the website's original wording so the admin form isn't empty. Never overwrites.
     */
    private void backfillAboutExtras() {
        siteContentRepository.findBySectionKey("ABOUT").ifPresent(about -> {
            boolean changed = false;
            if (isBlank(about.getStatProjects())) { about.setStatProjects("120+"); changed = true; }
            if (isBlank(about.getStatClients())) { about.setStatClients("60+"); changed = true; }
            if (isBlank(about.getStatYears())) { about.setStatYears("8+"); changed = true; }
            if (isBlank(about.getFeature1Title()) && isBlank(about.getFeature1Text())) {
                about.setFeature1Title("Senior-Led Teams");
                about.setFeature1Text("Experienced engineers and designers on every project, not just at the pitch stage.");
                changed = true;
            }
            if (isBlank(about.getFeature2Title()) && isBlank(about.getFeature2Text())) {
                about.setFeature2Title("Transparent Process");
                about.setFeature2Text("Regular demos, clear timelines, and direct access to the people building your product.");
                changed = true;
            }
            if (isBlank(about.getFeature3Title()) && isBlank(about.getFeature3Text())) {
                about.setFeature3Title("Support That Continues");
                about.setFeature3Text("We stay involved after launch with monitoring, maintenance, and responsive support.");
                changed = true;
            }
            if (changed) {
                siteContentRepository.save(about);
            }
        });
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void seedServices() {
        if (serviceItemRepository.count() == 0) {
            serviceItemRepository.save(service("Web & App Development",
                    "Custom websites, e-commerce platforms, and cross-platform mobile apps built for performance and scale.",
                    "web", 1));
            serviceItemRepository.save(service("IT Consulting & Support",
                    "Managed IT services, infrastructure guidance, and round-the-clock helpdesk support for your team.",
                    "consulting", 2));
            serviceItemRepository.save(service("Software & SaaS Products",
                    "End-to-end custom software and multi-tenant SaaS builds, from architecture to deployment.",
                    "software", 3));
            serviceItemRepository.save(service("Digital Marketing & Design",
                    "SEO, branding, and UI/UX design that turns visitors into customers and products into brands.",
                    "marketing", 4));
        }
    }

    private ServiceItem service(String title, String description, String iconName, int order) {
        ServiceItem item = new ServiceItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setIconName(iconName);
        item.setDisplayOrder(order);
        item.setActive(true);
        return item;
    }

    private void seedContactInfo() {
        if (contactInfoRepository.count() == 0) {
            ContactInfo info = new ContactInfo();
            info.setEmail("akashingulkar245@gmail.com");
            info.setPhone("+91 98239 18904");
            info.setAddress("At Adavali, Post Karanjwane, Tal Velhe, Dist Pune");
            contactInfoRepository.save(info);
        }
    }
}
