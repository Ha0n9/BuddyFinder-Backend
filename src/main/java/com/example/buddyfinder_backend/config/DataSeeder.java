// Create seed data for testing
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
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@RequiredArgsConstructor
//public class DataSeeder {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Bean
//    public CommandLineRunner seedData() {
//        return args -> {
//            // Check if data already exists
//            if (userRepository.count() > 20) {
//                System.out.println("Data already seeded!");
//                return;
//            }
//
//            System.out.println("Seeding database with dummy users...");
//
//            List<User> users = Arrays.asList(
//                    User.builder()
//                            .name("Sarah Johnson")
//                            .email("sarah@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(25)
//                            .gender("Female")
//                            .interests("Yoga, Pilates, Running")
//                            .location("Vancouver")
//                            .availability(true)
//                            .bio("Yoga enthusiast 🧘‍♀️ Looking for workout buddies!")
//                            .tier(User.TierType.FREE)
//                            .zodiacSign("Gemini")
//                            .mbtiType("ENFP")
//                            .fitnessLevel("Intermediate")
//                            .isActive(true)
//                            .isVerified(false)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Mike Chen")
//                            .email("mike@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(28)
//                            .gender("Male")
//                            .interests("Weight lifting, Boxing, Gym")
//                            .location("Toronto")
//                            .availability(true)
//                            .bio("Gym rat 💪 Let's lift together!")
//                            .tier(User.TierType.PREMIUM)
//                            .zodiacSign("Leo")
//                            .mbtiType("ISTJ")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(true)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Emma Wilson")
//                            .email("emma@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(23)
//                            .gender("Female")
//                            .interests("Running, Cycling, Swimming")
//                            .location("Ha Noi")
//                            .availability(true)
//                            .bio("Marathon runner 🏃‍♀️ Training for my next race!")
//                            .tier(User.TierType.FREE)
//                            .zodiacSign("Virgo")
//                            .mbtiType("INFJ")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(false)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("David Park")
//                            .email("david@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(30)
//                            .gender("Male")
//                            .interests("CrossFit, Gym, Running")
//                            .location("Vancouver")
//                            .availability(true)
//                            .bio("CrossFit junkie! WOD partner needed 🏋️")
//                            .tier(User.TierType.ELITE)
//                            .zodiacSign("Scorpio")
//                            .mbtiType("ENTJ")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(true)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Lisa Anderson")
//                            .email("lisa@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(26)
//                            .gender("Female")
//                            .interests("Yoga, Meditation, Hiking")
//                            .location("Ha Noi")
//                            .availability(true)
//                            .bio("Wellness coach 🌿 Mind & body balance")
//                            .tier(User.TierType.FREE)
//                            .zodiacSign("Pisces")
//                            .mbtiType("INFP")
//                            .fitnessLevel("Intermediate")
//                            .isActive(true)
//                            .isVerified(false)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Alex Martinez")
//                            .email("alex@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(27)
//                            .gender("Male")
//                            .interests("Basketball, Gym, Swimming")
//                            .location("Toronto")
//                            .availability(true)
//                            .bio("Basketball player 🏀 Pickup games on weekends!")
//                            .tier(User.TierType.PREMIUM)
//                            .zodiacSign("Aries")
//                            .mbtiType("ESTP")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(true)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Sophie Taylor")
//                            .email("sophie@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(24)
//                            .gender("Female")
//                            .interests("Dance, Zumba, Cardio")
//                            .location("Vancouver")
//                            .availability(true)
//                            .bio("Dance fitness instructor 💃 Let's dance!")
//                            .tier(User.TierType.FREE)
//                            .zodiacSign("Libra")
//                            .mbtiType("ESFP")
//                            .fitnessLevel("Intermediate")
//                            .isActive(true)
//                            .isVerified(false)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Ryan Thompson")
//                            .email("ryan@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(29)
//                            .gender("Male")
//                            .interests("Rock climbing, Hiking, Gym")
//                            .location("Ha Noi")
//                            .availability(true)
//                            .bio("Adventure seeker 🧗 Climbing partner wanted!")
//                            .tier(User.TierType.FREE)
//                            .zodiacSign("Sagittarius")
//                            .mbtiType("ENFJ")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(false)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Olivia Brown")
//                            .email("olivia@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(22)
//                            .gender("Female")
//                            .interests("Tennis, Gym, Running")
//                            .location("Toronto")
//                            .availability(true)
//                            .bio("Tennis player 🎾 Looking for doubles partner!")
//                            .tier(User.TierType.PREMIUM)
//                            .zodiacSign("Taurus")
//                            .mbtiType("ISFJ")
//                            .fitnessLevel("Intermediate")
//                            .isActive(true)
//                            .isVerified(true)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("James Lee")
//                            .email("james@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(31)
//                            .gender("Male")
//                            .interests("Martial arts, Boxing, Gym")
//                            .location("Vancouver")
//                            .availability(true)
//                            .bio("MMA fighter 🥊 Training partners welcome!")
//                            .tier(User.TierType.ELITE)
//                            .zodiacSign("Capricorn")
//                            .mbtiType("ISTP")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(true)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Mia Garcia")
//                            .email("mia@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(25)
//                            .gender("Female")
//                            .interests("Cycling, Running, Triathlon")
//                            .location("Ha Noi")
//                            .availability(true)
//                            .bio("Triathlete 🚴‍♀️ Training for Ironman!")
//                            .tier(User.TierType.FREE)
//                            .zodiacSign("Cancer")
//                            .mbtiType("INTJ")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(false)
//                            .isAdmin(false)
//                            .build(),
//
//                    User.builder()
//                            .name("Chris Wang")
//                            .email("chris@example.com")
//                            .password(passwordEncoder.encode("123456"))
//                            .age(26)
//                            .gender("Male")
//                            .interests("Gym, Bodybuilding, Nutrition")
//                            .location("Toronto")
//                            .availability(true)
//                            .bio("Bodybuilder 💪 Nutrition coach available!")
//                            .tier(User.TierType.PREMIUM)
//                            .zodiacSign("Aquarius")
//                            .mbtiType("ENTP")
//                            .fitnessLevel("Advanced")
//                            .isActive(true)
//                            .isVerified(true)
//                            .isAdmin(false)
//                            .build()
//            );
//
//            userRepository.saveAll(users);
//            System.out.println("✅ Successfully seeded " + users.size() + " users!");
//            System.out.println("🔑 All users have password: 123456");
//        };
//    }
//}