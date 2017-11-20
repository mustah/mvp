import 'Logo.scss';
import * as React from 'react';
import {ClassNamed} from '../../types/Types';
import classNames = require('classnames');

export const Logo = (props: ClassNamed) => {
  return (
    <img src="elvaco_logo.png" className={classNames('EonLogo', props.className)}/>
  );
};
