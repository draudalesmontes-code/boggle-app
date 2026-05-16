import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, beforeEach, vi } from "vitest";
import SignupPage from "./SignupPage";

const mockNavigate = vi.fn();

// Override useNavigate so it can be tracked and tested within LoginPage
vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe("SignupPage", () => {
    // Run before every test to reset state
    beforeEach(() => {
        localStorage.clear();
        sessionStorage.clear();
        mockNavigate.mockClear();
        vi.restoreAllMocks();

        // Prevent alert popups during tests
        vi.spyOn(window, "alert").mockImplementation(() => {});
    });

    /**
     * Test 1: Page renders correctly
     * Verify that the signup form fields and create account button appear.
     */
    test("shows the signup page", () => {
        render(
        <MemoryRouter>
            <SignupPage />
        </MemoryRouter>
        );

        expect(screen.getByPlaceholderText(/username/i)).toBeTruthy();
        expect(screen.getByPlaceholderText(/email/i)).toBeTruthy();
        expect(screen.getByPlaceholderText(/^enter your password/i)).toBeTruthy();
        expect(screen.getByPlaceholderText(/confirm password/i)).toBeTruthy();
        expect(screen.getByRole("button", { name: /create account/i })).toBeTruthy();
    });

    /**
     * Test 2: Bad email should block signup
     * If the email format is invalid, the API should not be called.
     */
    test("does not call API for bad email", async () => {
        const fetchSpy = vi.spyOn(globalThis, "fetch");

        render(
        <MemoryRouter>
            <SignupPage />
        </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "bademail");
        await userEvent.type(screen.getByPlaceholderText(/^enter your password/i), "password123");
        await userEvent.type(screen.getByPlaceholderText(/confirm password/i), "password123");

        await userEvent.click(screen.getByRole("button", { name: /create account/i }));

        expect(fetchSpy).not.toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith("Invalid email");
    });

    /**
     * Test 3: Password mismatch should block signup
     * If password and confirm password differ, the API should not be called.
     */
    test("does not call API when passwords do not match", async () => {
        const fetchSpy = vi.spyOn(globalThis, "fetch");

        render(
        <MemoryRouter>
            <SignupPage />
        </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "user123@test.com");
        await userEvent.type(screen.getByPlaceholderText(/^enter your password/i), "password123");
        await userEvent.type(screen.getByPlaceholderText(/confirm password/i), "differentpass");

        await userEvent.click(screen.getByRole("button", { name: /create account/i }));

        expect(fetchSpy).not.toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith("Passwords do not match!");
    });

    /**
     * Test 4: Successful signup
     * Mock a successful API response, then verify navigation to home.
     */
    test("signs up and goes to home on success", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue({
            status: 201,
            json: vi.fn().mockResolvedValue({
                id: 1,
                username: "username123",
                email: "user123@test.com",
            }),
        });

        render(
        <MemoryRouter>
            <SignupPage />
        </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "user123@test.com");
        await userEvent.type(screen.getByPlaceholderText(/^enter your password/i), "password123");
        await userEvent.type(screen.getByPlaceholderText(/confirm password/i), "password123");

        await userEvent.click(screen.getByRole("button", { name: /create account/i }));

        await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith("/home");
        });
    });

    /**
     * Test 5: Failed signup should not navigate
     * Mock a conflict response and make sure the page does not go to home.
     */
    test("does not go to home on failed signup", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue({
            status: 409,
            json: vi.fn().mockResolvedValue({ message: "Username or email already taken" }),
        });

        render(
        <MemoryRouter>
            <SignupPage />
        </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "user123@test.com");
        await userEvent.type(screen.getByPlaceholderText(/^enter your password/i), "password123");
        await userEvent.type(screen.getByPlaceholderText(/confirm password/i), "password123");

        await userEvent.click(screen.getByRole("button", { name: /create account/i }));

        await waitFor(() => {
        expect(mockNavigate).not.toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith("Username or email already taken");
        });
    });

    /**
     * Test 6: Login link is present
     * Verify that users can move from signup back to login.
     */
    test("shows log in link", () => {
        render(
        <MemoryRouter>
            <SignupPage />
        </MemoryRouter>
        );

        expect(screen.getByRole("link", { name: /log in/i })).toBeTruthy();
    });
});