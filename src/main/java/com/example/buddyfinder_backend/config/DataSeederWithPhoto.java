//package com.example.buddyfinder_backend.config;
//
//import com.example.buddyfinder_backend.entity.Profile;
//import com.example.buddyfinder_backend.entity.User;
//import com.example.buddyfinder_backend.repository.ProfileRepository;
//import com.example.buddyfinder_backend.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@RequiredArgsConstructor
//public class DataSeederWithPhoto {
//
//    private final UserRepository userRepository;
//    private final ProfileRepository profileRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Bean
//    public CommandLineRunner seedData() {
//        return args -> {
//            if (userRepository.count() > 10) {
//                System.out.println("✅ Data already seeded!");
//                return;
//            }
//
//            System.out.println("🌱 Seeding database with users and photos...");
//
//            List<UserData> userData = Arrays.asList(
//                    new UserData("Sarah Johnson", "sarah@example.com", 25, "Female",
//                            "Yoga, Pilates, Running", "Vancouver", "Yoga enthusiast 🧘‍♀️",
//                            "Intermediate", "Gemini", "ENFP", User.TierType.FREE,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400",
//                                    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400"
//                            )),
//
//                    new UserData("Mike Chen", "mike@example.com", 28, "Male",
//                            "Weight lifting, Boxing, Gym", "Toronto", "Gym rat 💪",
//                            "Advanced", "Leo", "ISTJ", User.TierType.PREMIUM,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
//                                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400"
//                            )),
//
//                    new UserData("Emma Wilson", "emma@example.com", 23, "Female",
//                            "Running, Cycling, Swimming", "Ha Noi", "Marathon runner 🏃‍♀️",
//                            "Advanced", "Virgo", "INFJ", User.TierType.FREE,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400"
//                            )),
//
//                    new UserData("David Park", "david@example.com", 30, "Male",
//                            "CrossFit, Gym, Running", "Vancouver", "CrossFit junkie! 🏋️",
//                            "Advanced", "Scorpio", "ENTJ", User.TierType.ELITE,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
//                                    "https://images.unsplash.com/photo-1552374196-c4e7ffc6e126?w=400"
//                            )),
//
//                    new UserData("Lisa Anderson", "lisa@example.com", 26, "Female",
//                            "Yoga, Meditation, Hiking", "Ha Noi", "Wellness coach 🌿",
//                            "Intermediate", "Pisces", "INFP", User.TierType.FREE,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"
//                            )),
//
//                    new UserData("Alex Martinez", "alex@example.com", 27, "Male",
//                            "Basketball, Gym, Swimming", "Toronto", "Basketball player 🏀",
//                            "Advanced", "Aries", "ESTP", User.TierType.PREMIUM,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400"
//                            )),
//
//                    new UserData("Sophie Taylor", "sophie@example.com", 24, "Female",
//                            "Dance, Zumba, Cardio", "Vancouver", "Dance fitness instructor 💃",
//                            "Intermediate", "Libra", "ESFP", User.TierType.FREE,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400"
//                            )),
//
//                    new UserData("Ryan Thompson", "ryan@example.com", 29, "Male",
//                            "Rock climbing, Hiking, Gym", "Ha Noi", "Adventure seeker 🧗",
//                            "Advanced", "Sagittarius", "ENFJ", User.TierType.FREE,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1499996860823-5214fcc65f8f?w=400"
//                            )),
//
//                    new UserData("Olivia Brown", "olivia@example.com", 22, "Female",
//                            "Tennis, Gym, Running", "Toronto", "Tennis player 🎾",
//                            "Intermediate", "Taurus", "ISFJ", User.TierType.PREMIUM,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=400"
//                            )),
//
//                    new UserData("James Lee", "james@example.com", 31, "Male",
//                            "Martial arts, Boxing, Gym", "Vancouver", "MMA fighter 🥊",
//                            "Advanced", "Capricorn", "ISTP", User.TierType.ELITE,
//                            Arrays.asList(
//                                    "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=400"
//                            ))
//            );
//
//            for (UserData data : userData) {
//                User user = User.builder()
//                        .name(data.name)
//                        .email(data.email)
//                        .password(passwordEncoder.encode("123456"))
//                        .age(data.age)
//                        .gender(data.gender)
//                        .interests(data.interests)
//                        .location(data.location)
//                        .availability(true)
//                        .bio(data.bio)
//                        .tier(data.tier)
//                        .zodiacSign(data.zodiacSign)
//                        .mbtiType(data.mbtiType)
//                        .fitnessLevel(data.fitnessLevel)
//                        .isActive(true)
//                        .isVerified(data.tier != User.TierType.FREE)
//                        .isAdmin(false)
//                        .build();
//
//                User savedUser = userRepository.save(user);
//
//                // Create profile with photos
//                Profile profile = Profile.builder()
//                        .user(savedUser)
//                        .photos(toJsonArray(data.photos))
//                        .build();
//
//                profileRepository.save(profile);
//            }
//
//            System.out.println("✅ Successfully seeded " + userData.size() + " users with photos!");
//            System.out.println("🔑 All users have password: 123456");
//        };
//    }
//
//    private String toJsonArray(List<String> list) {
//        if (list == null || list.isEmpty()) {
//            return "[]";
//        }
//        return "[\"" + String.join("\",\"", list) + "\"]";
//    }
//
//    private static class UserData {
//        String name, email, gender, interests, location, bio, fitnessLevel, zodiacSign, mbtiType;
//        int age;
//        User.TierType tier;
//        List<String> photos;
//
//        UserData(String name, String email, int age, String gender, String interests,
//                 String location, String bio, String fitnessLevel, String zodiacSign,
//                 String mbtiType, User.TierType tier, List<String> photos) {
//            this.name = name;
//            this.email = email;
//            this.age = age;
//            this.gender = gender;
//            this.interests = interests;
//            this.location = location;
//            this.bio = bio;
//            this.fitnessLevel = fitnessLevel;
//            this.zodiacSign = zodiacSign;
//            this.mbtiType = mbtiType;
//            this.tier = tier;
//            this.photos = photos;
//        }
//    }
//}