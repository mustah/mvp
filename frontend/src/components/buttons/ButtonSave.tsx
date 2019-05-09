import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {buttonStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ClassNamed} from '../../types/Types';

interface Props extends ClassNamed {
  type: 'submit';
  disabled?: boolean;
}

export const ButtonSave = (props: Props) => (
  <FlatButton
    {...props}
    label={firstUpperTranslated('save')}
    style={buttonStyle}
  />
);
