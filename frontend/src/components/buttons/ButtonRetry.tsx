import * as React from 'react';
import {firstUpperTranslated} from '../../services/translationService';
import {ClassNamed, ClickableEventHandler} from '../../types/Types';
import {ButtonPrimary} from './ButtonPrimary';

type Props = ClassNamed & ClickableEventHandler;

export const ButtonRetry = (props: Props) => (
  <ButtonPrimary
    {...props}
    label={firstUpperTranslated('retry')}
    style={{marginBottom: 24}}
  />
);
