import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {buttonStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ClassNamed, Clickable} from '../../types/Types';

interface ButtonSaveSubmitProps extends ClassNamed {
  type: 'submit';
}

type ButtonSaveOnClickProps = ClassNamed & Clickable;

type ButtonSaveProps = ButtonSaveSubmitProps | ButtonSaveOnClickProps;

export const ButtonSave = (props: ButtonSaveProps) => (
  <FlatButton
    {...props}
    label={firstUpperTranslated('save')}
    style={buttonStyle}
  />
);
