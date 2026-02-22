//
//  Constants.swift
//  PetSchedulingiOS
//

import Foundation

enum Constants {
    enum PetType {
        static let allTypes = ["dog", "cat", "bird", "fish", "rabbit", "hamster", "chicken", "cow", "goat", "sheep", "pig", "horse", "duck", "turkey", "donkey", "other"]
        
        static func displayName(for type: String) -> String {
            switch type {
            case "dog": return "Dog"
            case "cat": return "Cat"
            case "bird": return "Bird"
            case "fish": return "Fish"
            case "rabbit": return "Rabbit"
            case "hamster": return "Hamster"
            case "chicken": return "Chicken"
            case "cow": return "Cow"
            case "goat": return "Goat"
            case "sheep": return "Sheep"
            case "pig": return "Pig"
            case "horse": return "Horse"
            case "duck": return "Duck"
            case "turkey": return "Turkey"
            case "donkey": return "Donkey"
            case "other": return "Other"
            default: return type
            }
        }
    }
    
    enum TaskCategory {
        static let allCategories = ["feeding", "medication", "vet_visit", "grooming", "exercise", "training", "reminder", "other"]
        
        static func displayName(for category: String) -> String {
            switch category {
            case "feeding": return "Feeding"
            case "medication": return "Medication"
            case "vet_visit": return "Vet Visit"
            case "grooming": return "Grooming"
            case "exercise": return "Exercise"
            case "training": return "Training"
            case "reminder": return "Reminder"
            case "other": return "Other"
            default: return category
            }
        }
    }
    
    enum RecurrencePattern {
        static let allPatterns = ["none", "daily", "weekly", "monthly", "yearly", "custom"]
        
        static func displayName(for pattern: String) -> String {
            switch pattern {
            case "none": return "No Repeat"
            case "daily": return "Daily"
            case "weekly": return "Weekly"
            case "monthly": return "Monthly"
            case "yearly": return "Yearly"
            case "custom": return "Custom"
            default: return pattern
            }
        }
    }
    
    static let reminderTimes = [1, 2, 3, 5, 15, 30, 60, 120]
}
