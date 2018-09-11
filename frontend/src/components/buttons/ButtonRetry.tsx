import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {buttonStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ClassNamed, Clickable} from '../../types/Types';

type Props = ClassNamed & Clickable;

const style: React.CSSProperties = {...buttonStyle, marginBottom: 24};

export const ButtonRetry = (props: Props) => (
  <FlatButton
    {...props}
    label={firstUpperTranslated('retry')}
    style={style}
  />
);
