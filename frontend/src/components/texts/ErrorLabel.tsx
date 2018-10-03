import './ErrorLabel.scss';
import * as React from 'react';
import {Children, ClassNamed} from '../../types/Types';
import {Xsmall} from './Texts';

interface Props extends ClassNamed {
  children: Children;
  hasError?: boolean;
}

export const ErrorLabel = ({children, hasError}: Props) =>
  hasError ? <Xsmall className="ErrorLabel">{children}</Xsmall> : null;
