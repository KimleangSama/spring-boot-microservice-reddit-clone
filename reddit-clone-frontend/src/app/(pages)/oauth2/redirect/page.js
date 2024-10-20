'use client';

import localforage from "localforage";
import { redirect } from "next/navigation";
import { useSearchParams } from 'next/navigation'
import { useEffect } from "react";

export default function OAuth2Redirect () {
    const params = useSearchParams();
    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');
    const error = params.get('error');

    if (error) {
        return (
            <div>
                <h1 className="text-4xl text-red-500 font-bold text-center">Error: {error}</h1>
            </div>
        );
    }
    useEffect(() => {
        if (accessToken && refreshToken) {
            localforage.setItem('accessToken', accessToken);
            localforage.setItem('refreshToken', refreshToken);
            redirect('/');
        }
    }, [accessToken, refreshToken]);

    // Redirect to home page
    return (
        <div>
            <h1 className="text-4xl font-bold text-center">Redirecting...</h1>
        </div>
    );
}