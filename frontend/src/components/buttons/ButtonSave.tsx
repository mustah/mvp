import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {buttonStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ClassNamed} from '../../types/Types';
import classNames = require('classnames');

interface ButtonSaveSubmitProps extends ClassNamed {
  type: 'submit';
  disabled?: boolean;
}

export const ButtonSave = ({className, disabled, ...props}: ButtonSaveSubmitProps) => (
  <FlatButton
    {...props}
    disabled={disabled}
    label={firstUpperTranslated('save')}
    className={classNames(className, disabled ? 'disabled' : '')}
    style={buttonStyle}
  />
);
