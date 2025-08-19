import React from "react";
import AdminLoginForm from "./AdminLogin";

const AdminAuth = () => {
  return (
    <div className="flex justify-center items-center h-screen bg-gray-50">
      <div className="max-w-4xl border rounded-md px-5 py-20 bg-white shadow-lg">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-[#00927c] mb-2">
            Nosso Closet
          </h1>
          <p className="text-gray-600">Painel Administrativo</p>
        </div>
        <AdminLoginForm />
      </div>
    </div>
  );
};

export default AdminAuth;
