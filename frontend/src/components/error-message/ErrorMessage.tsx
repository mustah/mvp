import * as React from 'react';
import './ErrorMessage.scss';

interface ErrorMessageProps {
  message?: string;
}

export const ErrorMessage = ({message}: ErrorMessageProps) =>
  message && <div className="Error-message">{message}</div> || null;
