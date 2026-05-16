import React, { useEffect, useState } from "react";
import './DictionaryPage.css';

/*
 * DictionaryPage component
 * ------------------------
 * This component fetches a list of dictionary words from an API
 * when the page loads. It stores the words in state and displays
 * them in a table. While the data is loading, it shows a loading message.
 */
const DictionaryPage = () => {

    //State to store the list of words from the API
    const [words, setWords] = useState([]);

    //State to track whether data is still loading
    const [loading, setLoading] = useState(true);

    /*
     * useEffect hook
     * --------------
     * This runs once when the component is first rendered.
     * It fetches dictionary data from the backend API.
     * If successful, it updates the words state and stops loading.
     * If there's an error, it logs the error and also stops loading.
     */
    useEffect(() => {
        fetch("/api/dictionary/all") //Make request to API
            .then((res) => {
                //Check if the response is valid
                if (!res.ok) throw new Error("Network response was not ok");
                return res.json(); //Convert response to JSON
            })
            .then((data) => {
                setWords(data);      //Save fetched data into state
                setLoading(false);   //Stop loading
            })
            .catch((err) => {
                console.error("Error fetching dictionary:", err); //Log error
                setLoading(false);   //Stop loading even if error occurs
            });
    }, []); //Empty dependency array = runs only once on mount

    //If still loading, show a message instead of the table
    if (loading) return <p>Loading dictionary...</p>;

    return (
        <div className="dictionary-container">
            <h1>Dictionary</h1>

            {/* Table to display words and their point values */}
            <table>
                <thead>
                <tr>
                    <th>Word</th>
                    <th>Point Value</th>
                </tr>
                </thead>

                <tbody>
                {words.map((entry) => (
                    //Each row represents one word from the dictionary
                    //The key helps React efficiently update the list
                    <tr key={entry.word}>
                        <td>{entry.word}</td> {/*Display the word */}
                        <td>{entry.pointValue}</td> {/*Display its point value */}
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

//Export the component so it can be used in other files
export default DictionaryPage;