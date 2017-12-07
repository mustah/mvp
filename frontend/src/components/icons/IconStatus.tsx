import * as classNames from 'classnames';
import * as React from 'react';
import {IdNamed, statusFor} from '../../types/Types';
import './IconStatus.scss';

export const IconStatus = ({id, name}: IdNamed) => {
  const status = statusFor(id);
  return status && <div className={classNames('IconStatus', status)}>{name}</div>;
};
