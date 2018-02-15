import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {buttonStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ClassNamed, Clickable} from '../../types/Types';

type Props = ClassNamed & Clickable;

export const ButtonRetry = (props: Props) => (
  <FlatButton
    {...props}
    label={firstUpperTranslated('retry')}
    style={buttonStyle}
  />
);
