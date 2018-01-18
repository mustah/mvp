import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {buttonStyle} from '../../app/themes';
import {translate} from '../../services/translationService';
import {ClassNamed} from '../../types/Types';

interface ButtonLoginProps extends ClassNamed {
  fullWidth: boolean;
  type: 'submit';
}

export const ButtonLogin = (props: ButtonLoginProps) => (
  <FlatButton
    {...props}
    label={translate('login')}
    style={buttonStyle}
  />
);
