import * as React from 'react';
import {Styled} from '../../types/Types';
import './ErrorMessage.scss';

interface ErrorMessageProps extends Styled {
  message?: string;
}

export const ErrorMessage = ({message, style}: ErrorMessageProps) =>
  message && <div className="Error-message" style={style}>{message}</div> || null;
