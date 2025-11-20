//package com.example.buddyfinder_backend.config;
//
//import com.example.buddyfinder_backend.entity.User;
//import com.example.buddyfinder_backend.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Configuration
//@RequiredArgsConstructor
//public class DataSeeder {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    private static final List<SeedUser> DEFAULT_USERS = List.of(
//            new SeedUser(
//                    "Sarah Johnson",
//                    "sarah@example.com",
//                    "123456",
//                    25,
//                    "Female",
//                    "Yoga, Pilates, Running",
//                    "Vancouver, BC",
//                    49.2827,
//                    -123.1207,
//                    "Weekday mornings (6-9 AM)",
//                    "Yoga enthusiast looking for mindful workout buddies.",
//                    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1",
//                    User.TierType.FREE,
//                    "Gemini",
//                    "ENFP",
//                    "Intermediate",
//                    false,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Mike Chen",
//                    "mike@example.com",
//                    "123456",
//                    28,
//                    "Male",
//                    "Weight lifting, Boxing, Gym",
//                    "Toronto, ON",
//                    43.65107,
//                    -79.347015,
//                    "Weekday evenings (6-9 PM)",
//                    "Strength coach who loves sharing form tips.",
//                    "https://images.unsplash.com/photo-1502767089025-6572583495b0",
//                    User.TierType.PREMIUM,
//                    "Leo",
//                    "ISTJ",
//                    "Advanced",
//                    true,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Emma Wilson",
//                    "emma@example.com",
//                    "123456",
//                    23,
//                    "Female",
//                    "Running, Cycling, Swimming",
//                    "Ha Noi, Vietnam",
//                    21.027763,
//                    105.83416,
//                    "Weekend mornings (7-10 AM)",
//                    "Marathon runner training for her next race.",
//                    "https://images.unsplash.com/photo-1544723795-3fb6469f5b39",
//                    User.TierType.FREE,
//                    "Virgo",
//                    "INFJ",
//                    "Advanced",
//                    false,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "David Park",
//                    "david@example.com",
//                    "123456",
//                    30,
//                    "Male",
//                    "CrossFit, HIIT, Running",
//                    "Vancouver, BC",
//                    49.246292,
//                    -123.116226,
//                    "Early mornings (5-7 AM)",
//                    "CrossFit junkie searching for accountability partners.",
//                    "https://images.unsplash.com/photo-1503467913725-8484b65b0715",
//                    User.TierType.ELITE,
//                    "Scorpio",
//                    "ENTJ",
//                    "Advanced",
//                    true,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Lisa Anderson",
//                    "lisa@example.com",
//                    "123456",
//                    27,
//                    "Female",
//                    "Pilates, Barre, Stretching",
//                    "Toronto, ON",
//                    43.642567,
//                    -79.387054,
//                    "Weekday lunch breaks",
//                    "Office worker squeezing in low-impact sessions downtown.",
//                    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1",
//                    User.TierType.FREE,
//                    "Libra",
//                    "ISFJ",
//                    "Beginner",
//                    false,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Kevin Tran",
//                    "kevin@example.com",
//                    "123456",
//                    26,
//                    "Male",
//                    "Muay Thai, Calisthenics, Swimming",
//                    "Ho Chi Minh City, Vietnam",
//                    10.823099,
//                    106.629662,
//                    "Late nights (8-10 PM)",
//                    "Night owl who loves intense cardio sessions.",
//                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
//                    User.TierType.PREMIUM,
//                    "Aquarius",
//                    "ENTP",
//                    "Advanced",
//                    true,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Olivia Brown",
//                    "olivia@example.com",
//                    "123456",
//                    29,
//                    "Female",
//                    "Spin, Strength Training, Hiking",
//                    "New York, NY",
//                    40.7128,
//                    -74.0060,
//                    "Weekend afternoons",
//                    "Corporate professional seeking balanced workouts.",
//                    "https://images.unsplash.com/photo-1521572267360-ee0c2909d518",
//                    User.TierType.FREE,
//                    "Capricorn",
//                    "ENFJ",
//                    "Intermediate",
//                    true,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Ethan Lee",
//                    "ethan@example.com",
//                    "123456",
//                    32,
//                    "Male",
//                    "Cycling, Trail Running, Yoga",
//                    "San Francisco, CA",
//                    37.7749,
//                    -122.4194,
//                    "Flexible evenings",
//                    "Product designer obsessed with Strava segments.",
//                    "https://images.unsplash.com/photo-1504593811423-6dd665756598",
//                    User.TierType.PREMIUM,
//                    "Sagittarius",
//                    "INTP",
//                    "Intermediate",
//                    true,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Ava Patel",
//                    "ava@example.com",
//                    "123456",
//                    24,
//                    "Female",
//                    "Dance, Barre, Functional Fitness",
//                    "Seattle, WA",
//                    47.6062,
//                    -122.3321,
//                    "Weekday evenings",
//                    "Former dancer transitioning into strength work.",
//                    "https://images.unsplash.com/photo-1517841905240-472988babdf9",
//                    User.TierType.FREE,
//                    "Pisces",
//                    "ISFP",
//                    "Beginner",
//                    false,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Noah Kim",
//                    "noah@example.com",
//                    "123456",
//                    31,
//                    "Male",
//                    "Powerlifting, Mobility, Swimming",
//                    "Seoul, South Korea",
//                    37.5665,
//                    126.9780,
//                    "Early mornings",
//                    "Powerlifter adding mobility and recovery partners.",
//                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d",
//                    User.TierType.ELITE,
//                    "Taurus",
//                    "ISTP",
//                    "Advanced",
//                    true,
//                    false,
//                    false
//            ),
//            new SeedUser(
//                    "Chloe Nguyen",
//                    "chloe@example.com",
//                    "123456",
//                    22,
//                    "Female",
//                    "Swimming, Yoga, Meditation",
//                    "Ha Noi, Vietnam",
//                    21.0409,
//                    105.8342,
//                    "Weekday afternoons",
//                    "Student balancing classes with mindful movement.",
//                    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80",
//                    User.TierType.FREE,
//                    "Cancer",
//                    "INFP",
//                    "Beginner",
//                    false,
//                    false,
//                    false
//            )
//    );
//
//    @Bean
//    public CommandLineRunner seedData() {
//        return args -> {
//            long existingUsers = userRepository.count();
//            if (existingUsers > 0) {
//                System.out.printf("Skipping seeding: %d users already exist.%n", existingUsers);
//                return;
//            }
//
//            List<User> users = DEFAULT_USERS.stream()
//                    .map(seedUser -> seedUser.toEntity(passwordEncoder))
//                    .collect(Collectors.toList());
//
//            userRepository.saveAll(users);
//            System.out.printf("Seeded %d users (default password: 123456)%n", users.size());
//        };
//    }
//
//    private record SeedUser(
//            String name,
//            String email,
//            String password,
//            Integer age,
//            String gender,
//            String interests,
//            String location,
//            Double latitude,
//            Double longitude,
//            String availability,
//            String bio,
//            String profilePictureUrl,
//            User.TierType tier,
//            String zodiacSign,
//            String mbtiType,
//            String fitnessLevel,
//            boolean verified,
//            boolean admin,
//            boolean isSuperAdmin
//    ) {
//        User toEntity(PasswordEncoder passwordEncoder) {
//            return User.builder()
//                    .name(name)
//                    .email(email.toLowerCase())
//                    .password(passwordEncoder.encode(password))
//                    .age(age)
//                    .gender(gender)
//                    .interests(interests)
//                    .location(location)
//                    .latitude(latitude != null ? latitude.floatValue() : null)
//                    .longitude(longitude != null ? longitude.floatValue() : null)
//                    .availability(availability)
//                    .bio(bio)
//                    .profilePictureUrl(profilePictureUrl)
//                    .tier(tier)
//                    .zodiacSign(zodiacSign)
//                    .mbtiType(mbtiType)
//                    .fitnessLevel(fitnessLevel)
//                    .isVerified(verified)
//                    .isAdmin(admin)
//                    .isActive(true)
//                    .isSuperAdmin(isSuperAdmin)
//                    .build();
//        }
//    }
//}
