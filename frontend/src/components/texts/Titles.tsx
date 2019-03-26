import * as React from 'react';
import {Row} from '../layouts/row/Row';
import {Medium, Small, Xlarge} from './Texts';
import './Titles.scss';

interface TitleProps {
  children: string;
  className?: string;
  subtitle?: string;
}

export const MainTitle = ({children, subtitle}: TitleProps) => (
  <Row className="MainTitle">
    <Xlarge className="Bold">{children}</Xlarge>
    {subtitle && <Small className="Subtitle-description">{subtitle}</Small>}
  </Row>
);

export const Subtitle = ({children}: TitleProps) => (
  <Row className="Subtitle">
    <Medium className="Bold first-uppercase">{children}</Medium>
  </Row>
);

export const WidgetTitle = ({children}: TitleProps) => (
  <Row className="Subtitle">
    <Medium className="Subtitle-text ellipsis Bold first-uppercase" title={children}>{children}</Medium>
  </Row>
);
