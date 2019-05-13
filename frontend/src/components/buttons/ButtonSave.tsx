import * as React from 'react';
import {firstUpperTranslated} from '../../services/translationService';
import {ButtonPrimary} from './ButtonPrimary';
import FlatButtonProps = __MaterialUI.FlatButtonProps;

export const ButtonSave = (props: FlatButtonProps) => <ButtonPrimary {...props} label={firstUpperTranslated('save')}/>;
