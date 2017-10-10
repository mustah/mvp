export const normalizedValidationData = {
  meteringPoints: {
    byId: {
      '1234 1234 1234': {
        id: '1234 1234 1234',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'XX',
        status: {
          code: 0,
          text: 'ok',
        },
      },
      '1234 1234 1235': {
        id: '1234 1234 1235',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'XX',
        status: {
          code: 2,
          text: 'mätare går baklänges',
        },
      },
      '1234 1234 1236': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'XX',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1237': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1238': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1239': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1240': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1241': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1242': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
    },
    allIds: ['1234 1234 1234', '1234 1234 1235', '1234 1234 1236', '1234 1234 1237', '1234 1234 1238', '1234 1234 1239',
      '1234 1234 1240', '1234 1234 1241', '1234 1234 1242'],
  },
};

export const metersByGateway = {
  byGateway: {
    XX: {
      '1234 1234 1234': {
        id: '1234 1234 1234',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'XX',
        status: {
          code: 0,
          text: 'ok',
        },
      },
      '1234 1234 1235': {
        id: '1234 1234 1235',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'XX',
        status: {
          code: 2,
          text: 'mätare går baklänges',
        },
      },
      '1234 1234 1236': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'XX',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
    },
    YY: {
      '1234 1234 1237': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1238': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1239': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1240': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1241': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
      '1234 1234 1242': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'mätare går inte alls',
        },
      },
    },
  },
  allIds: ['XX', 'YY'],
};

export const gateways = {
  byId: {
    XX: {
      id: 'XX',
      type: 'CMe3100',
      connectedMeters: 3,
      status: {
        code: 11,
        text: 'dålig uppkoppling',
      },
      action: 'felanmäld',
    },
    YY: {
      id: 'YY',
      type: 'CMe2100',
      connectedMeters: 5,
      status:
        {
          code: 10,
          text: 'ok',
        },
      action: '',
    },
  },
  allIds: ['XX', 'YY'],
};
