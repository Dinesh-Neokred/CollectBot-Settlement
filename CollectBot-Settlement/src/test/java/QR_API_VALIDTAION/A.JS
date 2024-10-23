import { ZodAny, z } from "zod";
import { buildJsonSchemas } from "fastify-zod";
const createClientApiSchema = z.object({
  client_ref_id: z
    .string()
    .min(36, {
      message: "client_ref_id should be of 36 characters",
    })
    .max(36, {
      message: "client_ref_id should be of 36 characters",
    }),
  reseller_id: z
    .string()
    .min(36, {
      message: "reseller_id should be of 36 characters",
    })
    .max(36, {
      message: "reseller_id should be of 36 characters",
    }),
  created_by: z
    .string()
    .min(36, {
      message: "created_by should be of 36 characters",
    })
    .max(36, {
      message: "created_by should be of 36 characters",
    }),
  client_name: z.string().max(50, {
    message: "client_name should be less than 50 characters",
  }),
  mobile_number: z
    .string()
    .min(10, {
      message: "Mobile number should be 10 characters ex 9876543210",
    })
    .max(10, {
      message: "Mobile number should be 10 characters ex 9876543210",
    })
    .regex(new RegExp("^[0-9_]*$"), {
      message: "please provide a valid mobile number",
    }),
  email: z.string().regex(new RegExp("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"), {
    message: "please provide a valid email",
  }),
  status: z.enum(["true", "false"]),
  programs: z.any(),
});
const initiatePaymentRequestSchemaBody = z.object({
  amount: z.string().regex(new RegExp("^(?:[1-9][0-9]{0,4}.?|0?.)[0-9]{2}$"), {
    message: "amount should have two decimal value",
  }),
  mobile: z
    .string()
    .min(10, {
      message: "Mobile number should be 10 characters ex 9876543210",
    })
    .max(10, {
      message: "Mobile number should be 10 characters ex 9876543210",
    })
    .regex(new RegExp("^[0-9_]*$"), {
      message: "please provide a valid mobile number",
    }),
  mechanism: z.enum(["UPI", "NB", "INTENT", "MW", "OM", "PL"]),
  remark: z
    .string()
    .max(50, {
      message: "remark cannot be more than 50 characters",
    })
    .regex(new RegExp("^[a-zA-Z0-9 _]*$"), {
      message: "should contains only alphaNumeric",
    }),
  email: z.string().regex(new RegExp("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"), {
    message: "please provide a valid email",
  }),
  payerVa: z
    .string()
    .regex(new RegExp("^[a-zA-Z0-9]+([._-]?[a-zA-Z0-9]+)*@[a-zA-Z]{2,64}$"), {
      message: "please provide a proper valid upi Id",
    })
    .optional(),
  orderId: z
    .string()
    .regex(new RegExp("^[a-zA-Z0-9_-]*$"), { message: "should contains only alphaNumeric" })
    .min(30, {
      message: "Order Id should be between 30 to 36 characters",
    })
    .max(36, {
      message: "Order Id should be between 30 to 36 characters",
    })
    .optional(),
  bankCode: z
    .string()
    .max(10, {
      message: "Bank code should be less than 10 characters",
    })
    .optional(),
});
const initiatePaymentRequestSchemaHeaders = z.object({
  client_secret: z
    .string()
    .min(36, {
      message: "client_secret should be of 36 characters",
    })
    .max(36, {
      message: "client_secret should be of 36 characters",
    }),
  program_id: z
    .string()
    .min(36, {
      message: "program_id should be of 36 characters",
    })
    .max(36, {
      message: "program_id should be of 36 characters",
    }),
});
//export type QrGenerateBody = z.infer<typeof qrGenerateBodySchema>;
export const { schemas: apiSchemas, $ref } = buildJsonSchemas(
  {
    createClientApiSchema,
    initiatePaymentRequestSchemaBody,
    initiatePaymentRequestSchemaHeaders,
  },
  {
    errorMessages: true,
  },
);