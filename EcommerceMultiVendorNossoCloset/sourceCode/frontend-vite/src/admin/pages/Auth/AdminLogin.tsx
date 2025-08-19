/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  Button,
  CircularProgress,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useFormik } from "formik";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import { useNavigate } from "react-router-dom";
import {
  sendLoginSignupOtp,
  signin,
} from "../../../Redux Toolkit/Customer/AuthSlice";
import OTPInput from "../../../customer/components/OtpFild/OTPInput";

const AdminLoginForm = () => {
  const navigate = useNavigate();
  const [otp, setOtp] = useState("");
  const [isOtpSent, setIsOtpSent] = useState(false);
  const [timer, setTimer] = useState<number>(30);
  const [isTimerActive, setIsTimerActive] = useState<boolean>(false);
  const [loginMethod, setLoginMethod] = useState<"password" | "otp">(
    "password"
  );
  const dispatch = useAppDispatch();
  const { auth } = useAppSelector((store) => store);

  const formik = useFormik({
    initialValues: {
      email: "",
      password: "",
      otp: "",
    },

    onSubmit: (values: any) => {
      if (loginMethod === "password") {
        // Login com senha
        dispatch(
          signin({ email: values.email, password: values.password, navigate })
        );
      } else {
        // Login com OTP
        dispatch(signin({ email: values.email, otp, navigate }));
      }
      console.log("Form data:", values);
    },
  });

  const handleOtpChange = (otp: any) => {
    setOtp(otp);
  };

  const handleResendOTP = () => {
    dispatch(sendLoginSignupOtp({ email: "signing_" + formik.values.email }));
    console.log("Resend OTP");
    setTimer(30);
    setIsTimerActive(true);
  };

  const handleSentOtp = () => {
    setIsOtpSent(true);
    handleResendOTP();
  };

  const handleLogin = () => {
    formik.handleSubmit();
  };

  const handleLoginMethodChange = (
    event: any,
    newMethod: "password" | "otp" | null
  ) => {
    if (newMethod !== null) {
      setLoginMethod(newMethod);
      setIsOtpSent(false);
      setOtp("");
    }
  };

  useEffect(() => {
    let interval: any;

    if (isTimerActive) {
      interval = setInterval(() => {
        setTimer((prev) => {
          if (prev === 1) {
            clearInterval(interval);
            setIsTimerActive(false);
            return 30;
          }
          return prev - 1;
        });
      }, 1000);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isTimerActive]);

  return (
    <div>
      <h1 className="text-center font-bold text-xl text-primary-color pb-8">
        Login Administrativo
      </h1>

      {/* Toggle para escolher método de login */}
      <div className="mb-6">
        <ToggleButtonGroup
          value={loginMethod}
          exclusive
          onChange={handleLoginMethodChange}
          aria-label="método de login"
          className="w-full"
        >
          <ToggleButton value="password" aria-label="senha" className="flex-1">
            Senha
          </ToggleButton>
          <ToggleButton value="otp" aria-label="otp" className="flex-1">
            Código por Email
          </ToggleButton>
        </ToggleButtonGroup>
      </div>

      <form className="space-y-5">
        <TextField
          fullWidth
          name="email"
          label="Email"
          value={formik.values.email}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={formik.touched.email && Boolean(formik.errors.email)}
          helperText={
            formik.touched.email ? (formik.errors.email as string) : undefined
          }
        />

        {loginMethod === "password" && (
          <TextField
            fullWidth
            name="password"
            label="Senha"
            type="password"
            value={formik.values.password}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.password && Boolean(formik.errors.password)}
            helperText={
              formik.touched.password
                ? (formik.errors.password as string)
                : undefined
            }
          />
        )}

        {loginMethod === "otp" && (
          <>
            {!isOtpSent && (
              <Button
                disabled={auth.loading}
                fullWidth
                variant="contained"
                onClick={handleSentOtp}
                sx={{ py: "11px" }}
              >
                {auth.loading ? <CircularProgress /> : "Enviar Código"}
              </Button>
            )}

            {auth.otpSent && (
              <div className="space-y-2">
                <p className="font-medium text-sm">
                  Digite o código enviado para seu email
                </p>
                <OTPInput length={6} onChange={handleOtpChange} error={false} />
                <p className="text-xs space-x-2">
                  {isTimerActive ? (
                    <span>Reenviar código em {timer} segundos</span>
                  ) : (
                    <>
                      Não recebeu o código?{" "}
                      <span
                        onClick={handleResendOTP}
                        className="text-teal-600 cursor-pointer hover:text-teal-800 font-semibold"
                      >
                        Reenviar
                      </span>
                    </>
                  )}
                </p>
              </div>
            )}
          </>
        )}

        <Button
          disabled={auth.loading}
          onClick={handleLogin}
          fullWidth
          variant="contained"
          sx={{ py: "11px" }}
        >
          {auth.loading ? <CircularProgress /> : "Entrar"}
        </Button>
      </form>
    </div>
  );
};

export default AdminLoginForm;
