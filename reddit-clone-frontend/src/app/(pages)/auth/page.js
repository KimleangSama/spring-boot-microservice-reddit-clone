import Auth from "@/app/components/auth/auth";
import Link from "next/link";

import { BsArrowLeft } from "react-icons/bs";

export default function Authentication() {
  return (
    <div className="overflow-hidden h-screen flex justify-center items-center">
        {/* Add back to home page button with back arrow icon */}
        <Link href="/" className="absolute top-0 left-0 p-4 flex items-center">
            <BsArrowLeft className="text-2xl" />
            <span className="mx-2 font-semibold text-lg">Back</span>
        </Link>
        <Auth />
    </div>
  );
}
