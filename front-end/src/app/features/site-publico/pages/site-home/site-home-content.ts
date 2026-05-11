// Conteúdo estático do MVP da landing page da Padaria Nova Aliança.
// Futuramente, parte desses dados poderá vir do back-end/admin para permitir edições dinâmicas.

export interface FeaturedProduct {
  id: string;
  name: string;
  description: string;
  icon: string;
}

export interface OrderCategory {
  name: string;
  description: string;
  emoji: string;
}

export interface BenefitItem {
  icon: string;
  title: string;
  description: string;
}

export const featuredProducts: FeaturedProduct[] = [
  {
    id: 'pao-frances',
    name: 'Pão Francês',
    description: 'Crocante por fora, macio por dentro. Feito diariamente com ingredientes frescos.',
    icon: '🥖'
  },
  {
    id: 'misto-quente',
    name: 'Misto Quente',
    description: 'Sanduíche quentinho com presunto e queijo. Perfeito para o café da manhã.',
    icon: '🥪'
  },
  {
    id: 'pao-queijo',
    name: 'Pão com Queijo',
    description: 'Delicioso pão recheado com queijo derretido. Uma combinação clássica.',
    icon: '🧀'
  },
  {
    id: 'cafe',
    name: 'Café',
    description: 'Café coado fresquinho, encorpado e com aroma incomparável.',
    icon: '☕'
  },
  {
    id: 'salgados',
    name: 'Salgados',
    description: 'Variedade de salgados crocantes: coxinha, pastel, empada e mais.',
    icon: '🥟'
  },
  {
    id: 'bolos-tortas',
    name: 'Bolos e Tortas',
    description: 'Sobremesas artesanais deliciosas. Chocolate, cenoura, morango e mais.',
    icon: '🎂'
  }
];

export const orderCategories: OrderCategory[] = [
  {
    name: 'Tortas',
    description: 'Tortas para aniversários, reuniões, cafés especiais e momentos em família.',
    emoji: '🎂'
  },
  {
    name: 'Bolos',
    description: 'Bolos para o dia a dia ou para deixar sua comemoração mais completa.',
    emoji: '🍰'
  },
  {
    name: 'Rocamboles',
    description: 'Rocamboles doces para servir em fatias, presentear ou compartilhar.',
    emoji: '🍥'
  },
  {
    name: 'Salgados',
    description: 'Salgados para festas, encontros, reuniões e pedidos maiores.',
    emoji: '🥟'
  },
  {
    name: 'Kits de café da manhã',
    description: 'Combinações para presentear ou montar um café especial em casa.',
    emoji: '☕'
  },
  {
    name: 'Doces',
    description: 'Doces para complementar sua mesa, festa ou encomenda especial.',
    emoji: '🧁'
  }
];

export const benefits: BenefitItem[] = [
  {
    icon: '🥖',
    title: 'Produtos frescos todos os dias',
    description:
      'Pães, lanches e produtos preparados para acompanhar o café da manhã, a tarde e a rotina da família.'
  },
  {
    icon: '☕',
    title: 'Café e lanches preparados na hora',
    description:
      'Opções rápidas para quem quer tomar um café, fazer um lanche ou levar algo gostoso para casa.'
  },
  {
    icon: '🎂',
    title: 'Bolos e tortas para encomenda',
    description:
      'Encomendas para festas, cafés especiais, reuniões e momentos importantes.'
  },
  {
    icon: '💬',
    title: 'Atendimento por WhatsApp',
    description:
      'Tire dúvidas, consulte produtos e faça pedidos diretamente pelo WhatsApp da padaria.'
  },
  {
    icon: '📍',
    title: 'Fácil acesso em Parque Veneza, Magé',
    description:
      'A Nova Aliança fica na Estr. Mineira, 703, em Parque Veneza, Magé.'
  }
];
