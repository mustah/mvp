export interface BaseData {
  type: string;
  value: string;
  isSelected: boolean;
}

export interface DataTree extends BaseData {
  isOpen: boolean;
  childNodesWithSelections: string[];
  childNodes: PropertyCollection[];
}

export type PropertyCollection = BaseData | DataTree;

export const organizedData: DataTree[] = [
  {
    type: 'region',
    value: 'north',
    isOpen: false,
    isSelected: false,
    childNodesWithSelections: [],
    childNodes: [
      {
        type: 'city',
        value: 'luleå',
        isOpen: false,
        isSelected: false,
        childNodesWithSelections: [],
        childNodes: [
          {
            type: 'residential area',
            value: 'centrum',
            isOpen: false,
            isSelected: false,
            childNodesWithSelections: [],
            childNodes: [
              {
                type: 'property',
                value: 'storgatan 1',
                isOpen: false,
                isSelected: false,
                childNodesWithSelections: [],
                childNodes: [
                  {
                    type: 'moid',
                    value: 'moid1',
                  },
                  {
                    type: 'apartment',
                    value: 'apartment 1',
                    isOpen: false,
                    isSelected: false,
                    childNodesWithSelections: [],
                    childNodes: [
                      {
                        type: 'moid',
                        value: 'moid2',
                      },
                      {
                        type: 'moid',
                        value: 'moid3',
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    type: 'region',
    value: 'west',
    isOpen: false,
    isSelected: false,
    childNodesWithSelections: [],
    childNodes: [
      {
        type: 'city',
        value: 'göteborg',
        isOpen: false,
        isSelected: false,
        childNodesWithSelections: [],
        childNodes: [
          {
            type: 'residential area',
            value: 'centrum',
            isOpen: false,
            isSelected: false,
            childNodesWithSelections: [],
            childNodes: [
              {
                type: 'property',
                value: 'storgatan 1',
                isOpen: false,
                isSelected: false,
                childNodesWithSelections: [],
                childNodes: [
                  {
                    type: 'moid',
                    value: 'moid4',
                  },
                  {
                    type: 'apartment',
                    value: 'apartment 1',
                    isOpen: false,
                    isSelected: false,
                    childNodesWithSelections: [],
                    childNodes: [
                      {
                        type: 'moid',
                        value: 'moid5',
                      },
                      {
                        type: 'moid',
                        value: 'moid6',
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
];
