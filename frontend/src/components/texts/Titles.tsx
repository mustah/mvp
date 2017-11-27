import * as React from 'react';
import {Row} from '../layouts/row/Row';
import {Bold, Small, Xlarge} from './Texts';
import './Titles.scss';

interface TitleProps {
  children: React.ReactNode[] | React.ReactNode;
  className?: string;
  subtitle?: string;
}

export const MainTitle = (props: TitleProps) => (
  <Row className="MainTitle">
    <Xlarge className="Bold">{props.children}</Xlarge>
    {props.subtitle && <Small className="Subtitle-description">{props.subtitle}</Small>}
  </Row>
);

export const Subtitle = (props: TitleProps) => (
  <Row className="Subtitle">
    <Bold>{props.children}</Bold>
  </Row>
);
