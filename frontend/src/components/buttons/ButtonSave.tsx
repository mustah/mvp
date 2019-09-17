import * as React from 'react';
import {classes} from 'typestyle';
import {firstUpperTranslated} from '../../services/translationService';
import {ButtonPrimary} from './ButtonPrimary';
import FlatButtonProps = __MaterialUI.FlatButtonProps;

export const ButtonSave = (props: FlatButtonProps) => (
  <ButtonPrimary
    {...props}
    className={classes('flex-align-self-start', props.className)}
    label={firstUpperTranslated('save')}
  />
);
