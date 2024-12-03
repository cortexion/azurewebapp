import React from 'react'
import { useOutlet } from "react-router-dom";
import TopBar from "./TopBar";

export const HomeLayout = () => {
    const outlet = useOutlet();

    return (
        <div>
            <TopBar />
            {outlet}
        </div>
    );
};