import * as React from 'react';
import {ClassNamed} from '../../types/Types';
import './Logo.scss';
import classNames = require('classnames');

export const Logo = ({className, src}: ClassNamed & {src: string}) => {
  return (
    <img src={src} className={classNames('Logo', className)}/>
  );
};
