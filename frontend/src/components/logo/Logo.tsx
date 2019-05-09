import {default as classNames} from 'classnames';
import * as React from 'react';
import {ClassNamed} from '../../types/Types';
import './Logo.scss';

export const Logo = ({className, src}: ClassNamed & {src: string}) => (
  <img src={src} className={classNames('Logo', className)} alt="Evo"/>
);
