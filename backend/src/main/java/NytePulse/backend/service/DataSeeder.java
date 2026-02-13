package NytePulse.backend.service;

import NytePulse.backend.entity.ReservedUser;
import NytePulse.backend.repository.ReservedUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ReservedUserRepository repository;

    public DataSeeder(ReservedUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        String[] rawUsers = {
                "@JoeBiden", "@DonaldTrump", "@BarackObama", "@NarendraModi", "@RishiSunak",
                "@EmmanuelMacron", "@VladimirPutin", "@XiJinping", "@JustinTrudeau", "@OlafScholz",
                "@VolodymyrZelenskyy", "@RecepTayyipErdogan", "@KingCharles", "@PopeFrancis", "@ElonMusk",
                "@MarkZuckerberg", "@BillGates", "@JeffBezos", "@SamAltman", "@LarryPage",
                "@SergeyBrin", "@TimCook", "@SatyaNadella", "@SundarPichai", "@JensenHuang",
                "@Drake", "@TaylorSwift", "@TheWeeknd", "@BadBunny", "@BTS",
                "@Rihanna", "@Beyonce", "@ArianaGrande", "@JustinBieber", "@EdSheeran",
                "@BrunoMars", "@CalvinHarris", "@DavidGuetta", "@MartinGarrix", "@Tiesto",
                "@ArminVanBuuren", "@Skrillex", "@Diplo", "@SteveAoki", "@Marshmello",
                "@LeonardoDiCaprio", "@DwayneJohnson", "@Zendaya", "@TomCruise", "@ChrisHemsworth",
                "@RobertDowneyJr", "@CristianoRonaldo", "@LionelMessi", "@Neymar", "@KylianMbappe",
                "@LeBronJames", "@StephenCurry", "@ConorMcGregor", "@MaxVerstappen", "@LewisHamilton",
                "@ViratKohli", "@RogerFederer", "@Hakkasan", "@Ushuaia", "@Berghain",
                "@Zouk", "@Omnia", "@Pacha", "@Marquee", "@E11evenMiami",
                "@XSLasVegas", "@OctagonSeoul", "@CaesarsPalace", "@MGMGrand", "@Bellagio",
                "@WynnLasVegas", "@VenetianLasVegas", "@ResortsWorld", "@AtlantisBahamas", "@GreyGoose",
                "@Hennessy", "@JackDaniels", "@RedBull", "@AbsolutVodka", "@MoetChandon",
                "@PatronTequila", "@Heineken", "@Budweiser", "@CoronaBeer", "@DonJulio",
                "@Tomorrowland", "@UltraMusicFestival", "@Coachella", "@EDCLasVegas", "@Lollapalooza",
                "@BurningMan", "@admin", "@support", "@moderator", "@official",
                "@verified", "@nytepulse", "@team", "@security", "@staff", "@founder"
        };

        List<ReservedUser> usersToSave = Arrays.stream(rawUsers)
                .map(this::cleanUsername) // Remove @
                .filter(name -> !repository.existsByUsername(name)) // Avoid duplicates
                .map(ReservedUser::new)
                .collect(Collectors.toList());

        if (!usersToSave.isEmpty()) {
            repository.saveAll(usersToSave);
            System.out.println("Successfully saved " + usersToSave.size() + " reserved usernames.");
        } else {
            System.out.println("Usernames already exist in database.");
        }
    }

    private String cleanUsername(String rawName) {
        if (rawName != null && rawName.startsWith("@")) {
            return rawName.substring(1); // Remove the first character
        }
        return rawName;
    }
}