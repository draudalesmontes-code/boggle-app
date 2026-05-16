import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, test, expect, beforeEach, vi } from "vitest";
import LoginPage from "./LoginPage";

const mockNavigate = vi.fn();

// Override useNavigate so it can be tracked and tested within LoginPage
vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual("react-router-dom");
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe("LoginPage", () => {
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
     * Test 1: Check that the page renders correctly
     * Verify that the key inputs and button exist
     * If these fail the page isnt rendering properly
    */
    test("shows the login page", () => {
        render(
        <MemoryRouter>
            <LoginPage />
        </MemoryRouter>
        );

        expect(screen.getByPlaceholderText(/username/i)).toBeTruthy();
        expect(screen.getByPlaceholderText(/email/i)).toBeTruthy();
        expect(screen.getByPlaceholderText(/password/i)).toBeTruthy();
        expect(screen.getByRole("button", { name: /log in/i })).toBeTruthy();
    });

    /**
     * Test 2: Invalid email should block login
     * Simulate a user entering a bad email and clicking login
     * Api shoudnt be caleed and alert should throw an error
     */
    test("does not call API for bad email", async () => {
        // Track if fetch is called
        const fetchSpy = vi.spyOn(globalThis, "fetch");

        render(
        <MemoryRouter>
            <LoginPage />
        </MemoryRouter>
        );

        // Simulate typing inputs and clicking login button
        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "bademail");
        await userEvent.type(screen.getByPlaceholderText(/password/i), "password123");

        await userEvent.click(screen.getByRole("button", { name: /log in/i }));

        expect(fetchSpy).not.toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith("Invalid email");
    });


    /**
     * Test 3: Successful login
     * We mock the API returning a successful response
     * User shoudl be saved and app navigates to home
     */
    test("logs in and goes to home on success", async () => {

        // Replace fetch with fake success return
        vi.spyOn(globalThis, "fetch").mockResolvedValue({
            status: 200,
            json: vi.fn().mockResolvedValue({
            id: 1,
            username: "username123",
            email: "user123@test.com",
            }),
        });

        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "user123@test.com");
        await userEvent.type(screen.getByPlaceholderText(/password/i), "password123");

        await userEvent.click(screen.getByRole("button", { name: /log in/i }));

        // Wait for login to complete
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith("/home");
        });
    });

    /**
     * Test 4: Failed login
     * Simulate the 401 API call to reject the login
     * Shouldnt direct userto home and should show error or fail
     */
    test("does not go to home on failed login", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue({
            status: 401,
            json: async () => ({ message: "Invalid credentials" }),
        });

        render(
        <MemoryRouter>
            <LoginPage />
        </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "user123@test.com");
        await userEvent.type(screen.getByPlaceholderText(/password/i), "wrongpass");

        await userEvent.click(screen.getByRole("button", { name: /log in/i }));

        await waitFor(() => {
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });

    /**
     * Test 5: Login button starts disabled
     * Before the user enters valid input, the login button should be disabled
     * This prevents empty or incomplete submissions
    */
    test("login button starts disabled", () => {
        render(
            <MemoryRouter>
            <LoginPage />
            </MemoryRouter>
        );

        expect(screen.getByRole("button", { name: /log in/i }).disabled).toBe(true);
    });

    /**
     * Test 6: Invalid username format shows inline error
     * Simulate typing a username with illegal characters
     * The page should show the username format error message
     */
    test("shows username format error for invalid username", async () => {
        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        await userEvent.type(
            screen.getByPlaceholderText(/username/i),
            "bad user!"
        );

        expect(
            screen.getByText(/only letters, numbers, and underscores allowed/i)
        ).toBeTruthy();
    });

    /**
     * Test 7: Short username is blocked
     * Simulate entering a username below the minimum length
     * The login request should not be sent and an alert should appear
     */
    test("blocks short username", async () => {
        const fetchSpy = vi.spyOn(globalThis, "fetch");

        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "abc");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "user@test.com");
        await userEvent.type(screen.getByPlaceholderText(/password/i), "password123");

        await userEvent.click(screen.getByRole("button", { name: /log in/i }));

        expect(fetchSpy).not.toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith("Username should be at least 5 characters.");
    });

    /**
     * Test 8: Short password is blocked
     * Simulate entering a password below the minimum length
     * The login request should not be sent and an alert should appear
     */
    test("blocks short password", async () => {
        const fetchSpy = vi.spyOn(globalThis, "fetch");

        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "username123");
        await userEvent.type(screen.getByPlaceholderText(/email/i), "user@test.com");
        await userEvent.type(screen.getByPlaceholderText(/password/i), "abc");

        await userEvent.click(screen.getByRole("button", { name: /log in/i }));

        expect(fetchSpy).not.toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith("Password should be at least 7 characters.");
    });

    /**
     * Test 9: Guest login succeeds
     * Mock a successful guest API response
     * The guest account should be saved and the page should navigate to home
     */
    test("guest login stores user and goes to home", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue({
            status: 201,
            json: async () => ({
            id: 99,
            username: "GuestName-ABCD",
            email: "guest@test.local",
            }),
        });

        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "user");

        await userEvent.click(screen.getByRole("button", { name: /play as guest/i }));

        await waitFor(() => {
            expect(localStorage.getItem("bbUser")).toBeTruthy();
            expect(mockNavigate).toHaveBeenCalledWith("/home");
        });
    });

    /**
     * Test 10: Guest login conflict
     * Simulate the API rejecting guest creation because the name is taken
     * The page should alert the user and should not navigate away
     */
    test("guest login shows taken-name alert on conflict", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue({
            status: 409,
            json: async () => ({}),
        });

        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        await userEvent.type(screen.getByPlaceholderText(/username/i), "user");
        await userEvent.click(screen.getByRole("button", { name: /play as guest/i }));

        await waitFor(() => {
            expect(window.alert).toHaveBeenCalledWith("Username or email already taken!");
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });

    /**
     * Test 11: Signup link is present
     * Verify that the page includes a link to account creation
     * This ensures users can move from login to signup
     */
    test("shows create account link", () => {
        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        expect(screen.getByRole("link", { name: /create an account/i })).toBeTruthy();
    });
});